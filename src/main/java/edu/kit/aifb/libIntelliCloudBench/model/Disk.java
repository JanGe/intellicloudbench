package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;

import org.jclouds.compute.domain.Volume;

public class Disk implements Serializable {
  private static final long serialVersionUID = -5445074447824286059L;

  private final float size;

	public Disk(Volume volume) {
	  this.size = volume.getSize();
  }

	public float getSize() {
	  return size;
  }

}
