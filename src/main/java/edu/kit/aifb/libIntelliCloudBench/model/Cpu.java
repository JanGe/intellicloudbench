package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;

import org.jclouds.compute.domain.Processor;

public class Cpu implements Serializable {
  private static final long serialVersionUID = 8931431427444757208L;

  private final double cores;
	private final double speed;

	public Cpu(Processor processor) {
	  this.cores = processor.getCores();
	  this.speed = processor.getSpeed();
  }

	public double getCores() {
  	return cores;
  }

	public double getSpeed() {
  	return speed;
  }
}
