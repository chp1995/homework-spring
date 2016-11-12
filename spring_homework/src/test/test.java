package test;

import factory.BeanFactory;


public class test {

    public static void main(String[] args) {
    	BeanFactory beanfactory = new BeanFactory();
    	beanfactory.read("bean.xml");
    	
    	boss boss = (boss) beanfactory.getBean("boss");
    	boss.tostring();
    	System.out.println(boss.tostring());
    }
}