package factory;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.dom4j.Attribute;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.ElementHandler;
import org.dom4j.ElementPath;
import org.dom4j.io.SAXReader;

public class BeanFactory {
	
	private Map<String,Object> beanMap=new HashMap<String,Object>();
	
	private void setFieldValue(Object obj, Field field, String value) {
		String fieldType=field.getType().getSimpleName();
		try{
			if("int".equals(fieldType)){
				field.setInt(obj, new Integer(value));
			}else if("float".equals(fieldType)){
				field.setFloat(obj, new Float(value));
			}else{
				field.set(obj,value);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void setFieldValue(Object obj, Field field, Object bean) {
		try {
			field.set(obj, bean);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public void read(String xmlUrl){
		//读取xml文件
		SAXReader  saxReader=new SAXReader();
		File file=new File(xmlUrl);
		try{
			saxReader.addHandler("/beans/bean",new BeanHandler());
			saxReader.read(file);
		}
		catch(DocumentException e){
			System.out.println(e.getMessage());
		}
	}
	
	class BeanHandler implements ElementHandler {

		private Object obj=null;
		
		public void onStart(ElementPath path) {
			
			Element beanElement=path.getCurrent();     
			Attribute classAttribute=beanElement.attribute("class");  
			
			Class<?> bean=null;
			
			try{
				bean=Class.forName(classAttribute.getText()); 
			}
			catch(ClassNotFoundException e){
				e.printStackTrace();
			}

			Field fields[]=bean.getDeclaredFields();
			Map<String,Field> fieldMap=new HashMap<String,Field>();
			

			for(Field field:fields){
				fieldMap.put(field.getName(), field);
			}
			
			try{
				obj=bean.newInstance(); 
			}
			catch(Exception e){
				e.printStackTrace();
			}
			

			path.addHandler("property", new PropertyHandler(fieldMap,obj));
		}
		public void onEnd(ElementPath path) {
			Element currentElement=path.getCurrent();
			Attribute idAttribute=currentElement.attribute("id");
			beanMap.put(idAttribute.getText(), obj);		
			path.removeHandler("property");		
		}
	}
	
	 
	class PropertyHandler implements ElementHandler {
		
		private Map<String,Field> fieldMap;
		private Object obj;
		
		public PropertyHandler(Map<String,Field> fieldMap,Object obj){
			this.fieldMap=fieldMap;
			this.obj=obj;		
		}

		public void onEnd(ElementPath path) {
			path.removeHandler("value");
			path.removeHandler("ref");
		}

		public void onStart(ElementPath path) {
			Element propertyElement=path.getCurrent();
			Attribute nameAttribute=propertyElement.attribute("name");
			path.addHandler("value", new ValueHandler(fieldMap,obj,nameAttribute));
			path.addHandler("ref", new RefHandler(fieldMap,obj,nameAttribute));

		}
	}
	
	
	class ValueHandler implements ElementHandler{
		//处理value
		private Map<String,Field> fieldMap;
		private Object obj;
		private Attribute nameAttribute;
		
		public ValueHandler(Map<String,Field> fieldMap,Object obj,Attribute nameAttribute){
			this.fieldMap=fieldMap;
			this.obj=obj;
			this.nameAttribute=nameAttribute;		
		}

		public void onEnd(ElementPath path) {
			Element valueElement=path.getCurrent();
			String strValue=valueElement.getText();
			Field tempField=fieldMap.get(nameAttribute.getValue());
			if(tempField!=null){
				tempField.setAccessible(true);
				setFieldValue(obj,tempField,strValue);
			}
		}
		public void onStart(ElementPath arg0) {
		}
	}

	
	class RefHandler implements ElementHandler{
		// 处理ref
		private Map<String,Field> fieldMap;
		private Object obj;
		private Attribute nameAttribute;
		private Object bean;
		
		public RefHandler(Map<String,Field> fieldMap,Object obj,Attribute nameAttribute){
			this.fieldMap=fieldMap;
			this.obj=obj;
			this.nameAttribute=nameAttribute;	
		}

		public void onEnd(ElementPath arg0) {
			Field tempField=fieldMap.get(nameAttribute.getValue());
			if(tempField!=null){
				tempField.setAccessible(true);				
				setFieldValue(obj,tempField,bean);
			}			
		}
		public void onStart(ElementPath path) {
			Element refElement=path.getCurrent();
			Attribute beanAttribute=refElement.attribute("bean");
			bean=getBean(beanAttribute.getValue());
		}		
	}
	
	public Object getBean(String beanName){
		Object obj=beanMap.get(beanName);
		return obj;		
	}
}
