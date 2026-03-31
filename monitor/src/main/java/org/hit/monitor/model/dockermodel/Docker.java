package org.hit.monitor.model.dockermodel;

import java.util.ArrayList;
import java.util.List;

public class Docker {
	private String id;
	private String name;
	private List<String> aliases=new ArrayList<String>();
	private String namespace;
	private Spec spec;
	private List<Stats> stats = new ArrayList<Stats>();
	
	public Docker(){
		stats =new ArrayList<Stats>(); 
	}
	
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<String> getAliases() {
		return aliases;
	}
	public void setAliases(List<String> aliases) {
		this.aliases = aliases;
	}
	public String getNamespace() {
		return namespace;
	}
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
	public Spec getSpec() {
		return spec;
	}
	public void setSpec(Spec spec) {
		this.spec = spec;
	}
	public List<Stats> getStats() {
		return stats;
	}
	public void setStats(List<Stats> stats) {
		this.stats = stats;
	}


	@Override
	public String toString() {
		return "Docker [id=" + id + ", name=" + name + ", aliases=" + aliases + ", namespace=" + namespace + ", spec="
				+ spec + ", stats=" + stats + "]";
	}
	
}
