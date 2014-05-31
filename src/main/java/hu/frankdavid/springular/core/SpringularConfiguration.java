package hu.frankdavid.springular.core;

import hu.frankdavid.springular.core.rpc.RPCSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;

@Configuration
@EnableWebMvc
@EnableWebSocket
@ComponentScan(basePackages = "hu.frankdavid.springular.core")
public class SpringularConfiguration extends WebSocketConfigurationSupport implements WebSocketConfigurer {

    @Autowired
    private RPCSocketHandler handler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/remote/socket");
    }

    @Bean
    public java.util.List<SpringularConfiguration> webSocketConfigurers() {
        return Arrays.asList(this);
    }
}
