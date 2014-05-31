package hu.frankdavid.springular.core.rpc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Component
public class RPCSocketHandler extends TextWebSocketHandler {

    @Autowired
    private ApplicationContext applicationContext;

    private ObjectMapper objectMapper;

    public RPCSocketHandler() {
        objectMapper = new ObjectMapper();
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String content = message.getPayload();

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rpc = objectMapper.reader().readTree(content);
        int id = rpc.get(0).asInt();
        try {
            Object result = processRPCMessage(rpc);
            String resultString = objectMapper.writer().writeValueAsString(new Object[]{id, result});
            session.sendMessage(new TextMessage(resultString));
        } catch (RPCException e) {
            e.printStackTrace();
        }


        super.handleTextMessage(session, message);
    }

    protected Object processRPCMessage(JsonNode rpcMessage) throws RPCException {
        Object bean;
        try {
            bean = applicationContext.getBean(rpcMessage.get(1).asText());
        } catch (BeansException e) {
            throw new RPCException(e);
        }

        for (Method method : bean.getClass().getMethods()) {
            JsonNode parameters = rpcMessage.get(3);
            if(method.getName().equals(rpcMessage.get(2).asText()) && method.getParameterTypes().length == parameters.size()) {
                try {
                    return invokeMethod(bean, method, parameters);
                } catch(IllegalAccessException | IOException e) {
                    //continue
                } catch (InvocationTargetException e) {
                    throw new RPCException(e.getCause());
                }
            }
        }
        throw new RPCException();
    }

    protected Object invokeMethod(Object bean, Method method, JsonNode parameters) throws InvocationTargetException, IllegalAccessException, IOException {
        Object[] args = new Object[parameters.size()];
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (int i = 0; i < parameters.size(); i++) {
            JsonNode parameter = parameters.get(i);
            Class<?> targetType = parameterTypes[i];
            Object result = objectMapper.reader(targetType).readValue(parameter);
            args[i] = result;
        }
        return method.invoke(bean, args);

    }
}
