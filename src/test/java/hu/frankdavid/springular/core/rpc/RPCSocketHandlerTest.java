package hu.frankdavid.springular.core.rpc;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
public class RPCSocketHandlerTest {

    @Autowired
    private RPCSocketHandler rpcHandler;

    @org.junit.Test
    public void testProcessRPCMessage() throws Exception {
        RPCMessage rpcMessage = new RPCMessage();
        rpcMessage.setBeanName("TestService");
        rpcMessage.setMethodName("getHello");
        rpcMessage.setParameters(Arrays.<Object>asList("David", "2"));

        Object result = rpcHandler.processRPCMessage(new ObjectMapper().readTree("[0, \"TestService\", \"getHello\", [\"David\", 21, {\"type\":\"Husky\", \"age\":3, \"color\":\"BLUE\"}]]"));
        assertEquals(result, "Hello David, you are 21 years old, your dog is a 3 year old blue Husky");

        Object result2 = rpcHandler.processRPCMessage(new ObjectMapper().readTree("[1, \"TestService\", \"getHello\", [\"David\"]]"));
        assertEquals(result2, "Hello David, no dogs today?");

        Object result3 = rpcHandler.processRPCMessage(new ObjectMapper().readTree("[2, \"TestService\", \"getHello\", [2, 3, 4]]"));
        assertEquals(result3, "Three integers");

    }

}