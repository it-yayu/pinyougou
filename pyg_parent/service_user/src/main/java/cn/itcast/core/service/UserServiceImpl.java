package cn.itcast.core.service;

import cn.itcast.core.dao.user.UserDao;
import cn.itcast.core.pojo.user.User;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {
    //注入redis模板
    @Autowired
    private RedisTemplate redisTemplate;

    //注入消息服务器模板
    @Autowired
    private JmsTemplate jmsTemplate;
    //注入消息服务器的发送目的地
    @Autowired
    private ActiveMQQueue smsDestination;
    @Autowired
    private UserDao userDao;

    @Value("${template_code}")
    private String template_code;
    @Value("${sign_name}")
    private String sign_name;

    /**
     * 根据手机号码发送验证码
     * @param phone
     */
    @Override
    public void sendCode(final String phone) {
        //1.接收手机号 随机生成六位数验证码
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            int sum = new Random().nextInt(10);
            sb.append(sum);
        }
        //2.将手机号作为key 验证码作为value存入redis数据库  设置生存时间为10分钟
        //(用hash存储不合适 hash存储为了节省key会用一个大的key 所有用户都用一个大key一个用户删除就所有用户就都没了)
        final String smsCode = sb.toString();
        redisTemplate.boundValueOps(phone).set(smsCode,60*10, TimeUnit.SECONDS);
        //3.将手机号 模板 签名 验证码 发送给消息服务器
        jmsTemplate.send(smsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                //发送内容应该为hash值
                MapMessage message = session.createMapMessage();
                message.setString("mobile", phone);//手机号
                message.setString("template_code", template_code);//模板编码
                message.setString("sign_name", sign_name);//签名
                Map map=new HashMap();
                map.put("code", smsCode);	//验证码
                System.out.println(map.get("code"));
                message.setString("param", JSON.toJSONString(map));
                return (Message) message;

            }
        });

    }

    /**
     * 判断注册是否成功
     * @param user
     * @param smscode
     * @return
     */
    @Override
    public Boolean checkSmsCode(User user, String smscode) {
        if(user==null||smscode==null||"".equals(user)||"".equals(smscode)){
            return false;
        }
        //取出redis中的验证码和注册时的比较
        String redisSmsCode= (String) redisTemplate.boundValueOps(user.getPhone()).get();
        //与注册时的验证码比较看是否一致,一致返回true不一致返回false
        if(redisSmsCode.equals(smscode)){
            return true;
        }
        return false;
    }

    /**
     * 保存用户
     * @param user
     */
    @Override
    public void add(User user) {
        userDao.insertSelective(user);
    }
}
