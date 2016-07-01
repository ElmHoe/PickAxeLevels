package uk.co.ElmHoe;

import java.util.List;

public class Tier {
	private int threshold;
	private boolean repair;
	private double money;
	private List<String> enchants;
	
	public Tier(int threshold, List<String> enchants, double money, boolean repair){
		this.threshold = threshold;
		this.repair = repair;
		this.money = money;
		this.enchants = enchants;
	}
	
	
	
	public List<String> getEnchants(){return enchants;}
	public boolean isRepaired(){return repair;}
	public double getMoney(){return money;}
	public int getThreshold(){return threshold;}
}
