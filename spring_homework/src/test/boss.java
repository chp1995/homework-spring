package test;

import factory.Autowired;

public class boss {
	 @Autowired
  private office office;
	 @Autowired
  private car car;
 
  
  	public office getoffice(){
  		return office;
  	} 
  	public void setoffice(office office){
  		this.office = office;
  	}
  	public car getcar(){
  		return car;
  	}
  	public void setcar(car car){
  		this.car=car;
  	}

  public String tostring(){
	  return "this boss has "+car.tostring()+"and in "+office.tostring();
  }
}
