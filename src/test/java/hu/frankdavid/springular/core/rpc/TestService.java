package hu.frankdavid.springular.core.rpc;

import org.springframework.stereotype.Service;

@Service("TestService")
public class TestService {
    public String getHello(String name, int age, Dog dog) {
        return String.format("Hello %s, you are %d years old, your dog is a %d year old %s %s",
                name, age, dog.getAge(), dog.getColor().toString().toLowerCase(), dog.getType());
    }

    public String getHello(String name) {
        return String.format("Hello %s, no dogs today?", name);
    }

    public String getHello(int a1, int a2, int a3) {
        return "Three integers";
    }
}
