package research;

import java.util.ArrayList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

public class CustomPair {
	List<Cloudlet> first = new ArrayList<Cloudlet>();
	List<Vm> second = new ArrayList<Vm>();
	public CustomPair(List<Cloudlet> finalcloudletList, List<Vm> finalvmlist){
		this.first = finalcloudletList;
		this.second = finalvmlist;
	}
	
	public List<Cloudlet> getFirst() {
		return first;
	}
	
	public List<Vm> getSecond() {
		return second;
	}
	
	public void setFirst(List<Cloudlet> first) {
		this.first = first;
	}
	
	public void setSecond(List<Vm> finalvmlist) {
		this.second = finalvmlist;
	}
}

class CustomDoublePair {
	
	/*
	 * @pre first = index
	 * @pre second = value stored : Double
	 * 
	 * */
	
	Integer first = 0;
	Double second = 0.0;
	public CustomDoublePair(Integer first, Double second){
		this.first = first;
		this.second = second;
	}
	
	public Integer getFirst() {
		return first;
	}
	
	public Double getSecond() {
		return second;
	}
	
	public void setFirst(Integer first) {
		this.first = first;
	}
	
	public void setSecond(Double second) {
		this.second = second;
	}
}

class CustomIntPair {
	
	/*
	 * @pre first = index
	 * @pre second = value stored : Integer
	 * 
	 * */
	
	Integer first = 0;
	Integer second = 0;
	public CustomIntPair(Integer first, Integer second){
		this.first = first;
		this.second = second;
	}
	
	public Integer getFirst() {
		return first;
	}
	
	public Integer getSecond() {
		return second;
	}
	
	public void setFirst(Integer first) {
		this.first = first;
	}
	
	public void setSecond(Integer second) {
		this.second = second;
	}
}

class CustomGenePair {
	
	/*
	 * @pre first = index
	 * @pre second = value stored : Integer
	 * 
	 * */
	
	Integer first = 0;
	Gene gene;
	
	public CustomGenePair(Integer first, Gene second){
		this.first = first;
		this.gene = second;
	}
	
	public Integer getFirst() {
		return first;
	}
	
	public Gene getSecond() {
		return gene;
	}
	
	public void setFirst(Integer first) {
		this.first = first;
	}
	
	public void setSecond(Gene second) {
		this.gene = second;
	}
}
