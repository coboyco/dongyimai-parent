import com.offcn.pojo.TbPayLog;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.ejb.interceptor.SpringBeanAutowiringInterceptor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath*:spring/applicationContext-*.xml")
public class PayLogTest {

    @Autowired
    private RedisTemplate redisTemplate;
    @Test
    public void test1(){
      TbPayLog payLog= (TbPayLog) redisTemplate.boundHashOps("payLog").get("test001");
        System.out.println(payLog.getTotalFee()+payLog.getUserId());
    }
}
