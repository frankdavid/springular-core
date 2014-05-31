package hu.frankdavid.springular.core.controller;

import hu.frankdavid.springular.core.rpc.EnableFromJavascript;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/remote")
public class RPCExporterController {

    @Autowired
    ApplicationContext applicationContext;

    /**
     * Exports service functions to javascript:
     * {@code
     * <script>
     * app.factory('Remote', function() {
     * var sc = new Websc();
     * function invoke(bean, method, params) {
     * sc.send(JSON.stringify([bean, method, params]));
     * }
     * return {
     * 'TestService': {
     * 'sayHello': function() {
     * invoke('TestService','sayHello', arguments);
     * }
     * }
     * }
     * });
     * </script>
     * }
     *
     * @return
     */
    @RequestMapping("/methods.js")
    @ResponseBody
    public String exportJS() {
        List<BeanInfo> beanInfoList = getBeanInfoList();
        return renderBeanInfoJavascript(beanInfoList);
    }

    protected List<BeanInfo> getBeanInfoList() {
        List<BeanInfo> beanInfoList = new ArrayList<>();
        for (Map.Entry<String, Object> entry : applicationContext.getBeansWithAnnotation(EnableFromJavascript.class).entrySet()) {
            Object bean = entry.getValue();
            BeanInfo beanInfo = new BeanInfo();
            beanInfo.setName(entry.getKey());
            Class<?> clazz = AopProxyUtils.ultimateTargetClass(bean);
            for (Method method : clazz.getMethods()) {
                if (method.getAnnotation(EnableFromJavascript.class) != null) {
                    beanInfo.getMethods().add(method.getName());
                }
            }
            if (beanInfo.getMethods().size() > 0)
                beanInfoList.add(beanInfo);
        }
        return beanInfoList;
    }

    protected String renderBeanInfoJavascript(List<BeanInfo> beanInfoList) {
        StringBuilder scriptBuilder = new StringBuilder();
        scriptBuilder
                .append("Remote=function(){")
                .append("var id=0;")
                .append("var cs={};")
                .append("var sc=new WebSocket('ws://localhost:8080/adweb/remote/socket');")
                .append("sc.onmessage=function(event){var o=JSON.parse(event.data);")
                .append("if(cs[o[0]]!=null){cs[o[0]](o[1]);delete cs[o[0]];}};")
                .append("function i(bean,method,args){")
                .append("cs[id]=args[args.length-1];")
                .append("sc.send(JSON.stringify([id++,bean,method,Array.prototype.slice.call(args,0,args.length-1)]));")
                .append("}")
                .append("return {");
        boolean firstBean = true;
        for (BeanInfo bean : beanInfoList) {
            boolean firstMethod = true;
            if(!firstBean)
                scriptBuilder.append(',');
            scriptBuilder.append("'").append(bean.getName()).append("':{");
            for (String method : bean.getMethods()) {
                if(!firstMethod)
                    scriptBuilder.append(',');
                scriptBuilder.append("'").append(method).append("':function(){")
                        .append("i('").append(bean.getName()).append("','").append(method).append("',arguments);")
                        .append("}");
                firstMethod = false;
            }
            scriptBuilder.append('}');
            firstBean = false;
        }
        scriptBuilder.append("}}();");
        return scriptBuilder.toString();
    }

    static class BeanInfo {
        private String name;

        private List<String> methods = new ArrayList<>();


        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public List<String> getMethods() {
            return methods;
        }
    }
}
