package example.service;

/**
 * @ClassDescription:
 * @Author: chensen
 * @Created: 2024/7/18 10:57
 */
public class SmsServiceImpl implements SmsService{
    @Override
    public String send(String message) {
        System.out.println("send message:" + message);
        return message;
    }
}
