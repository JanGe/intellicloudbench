package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jclouds.compute.domain.Hardware;
import org.jclouds.compute.domain.Processor;
import org.jclouds.compute.domain.Volume;

import edu.kit.aifb.IntelliCloudBench.util.StringUtils;

public class HardwareType implements Serializable, Comparable<HardwareType> {
	private static final long serialVersionUID = 8455234186001695579L;

	private String id;
	private String name;
	private int ram = 0;
	
	private List<Cpu> cpus = Collections.emptyList();
	private List<Disk> disks = Collections.emptyList();
	
	public HardwareType(Hardware hardware) {
		this.id = hardware.getId();
		this.name = hardware.getId();
		this.ram = hardware.getRam();

		cpus = new ArrayList<Cpu>(2);
		for (Processor processor : hardware.getProcessors()) {
			cpus.add(new Cpu(processor));
		}

		disks = new ArrayList<Disk>(5);
		for (Volume volume : hardware.getVolumes()) {
			disks.add(new Disk(volume));
		}
	}

	@Override
  public int hashCode() {
	  final int prime = 31;
	  int result = 1;
	  result = prime * result + ((id == null) ? 0 : id.hashCode());
	  return result;
  }

	@Override
  public boolean equals(Object obj) {
	  if (this == obj)
		  return true;
	  if (obj == null)
		  return false;
	  if (getClass() != obj.getClass())
		  return false;
	  HardwareType other = (HardwareType) obj;
	  if (id == null) {
		  if (other.id != null)
			  return false;
	  } else if (!id.equals(other.id))
		  return false;
	  return true;
  }

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public List<Cpu> getCpus() {
  	return cpus;
  }
	
	public String getCpusAsString() {
		String[] pairs = new String[cpus.size()];
		int i = 0;
		for (Cpu cpu : cpus) {
			pairs[i] = cpu.getCores() + " x " + cpu.getSpeed() + "GHz";
			i++;
		}
		return StringUtils.concatStringsWSep(Arrays.asList(pairs), " + ");
	}

	public int getRam() {
  	return ram;
  }
	
	public String getRamAsString() {
		return ram + " MB";
	}

	public List<Disk> getDisks() {
  	return disks;
  }

	public String getVolumesAsString() {
		int sum = 0;
		for (Disk disk : disks) {
			sum += disk.getSize();
		}
		return sum + " GB";
	}

	@Override
  public int compareTo(HardwareType anotherHardwareType) {
		return this.id.compareTo(anotherHardwareType.getId());
	}
}