/*
* This file is part of libIntelliCloudBench.
*
* Copyright (c) 2012, Jan Gerlinger <jan.gerlinger@gmx.de>
* All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions are met:
* * Redistributions of source code must retain the above copyright
* notice, this list of conditions and the following disclaimer.
* * Redistributions in binary form must reproduce the above copyright
* notice, this list of conditions and the following disclaimer in the
* documentation and/or other materials provided with the distribution.
* * Neither the name of the Institute of Applied Informatics and Formal
* Description Methods (AIFB) nor the names of its contributors may be used to
* endorse or promote products derived from this software without specific prior
* written permission.
*
* THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
* ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
* WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
* DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
* DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
* (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
* LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
* ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
* (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
* SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

package edu.kit.aifb.libIntelliCloudBench.model;

import java.io.Serializable;

public class InstanceType implements Serializable {
	private static final long serialVersionUID = -9139382679909105340L;

	private Provider provider;
	private Region region;
	private HardwareType hardwareType;

	public InstanceType(Provider provider, Region region, HardwareType hardwareType) {
		this.provider = provider;
		this.region = region;
		this.hardwareType = hardwareType;
	}

	public Provider getProvider() {
		return provider;
	}

	public Region getRegion() {
		return region;
	}

	public HardwareType getHardwareType() {
		return hardwareType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((hardwareType == null) ? 0 : hardwareType.hashCode());
		result = prime * result + ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + ((region == null) ? 0 : region.hashCode());
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
		InstanceType other = (InstanceType) obj;
		if (hardwareType == null) {
			if (other.hardwareType != null)
				return false;
		} else if (!hardwareType.equals(other.hardwareType))
			return false;
		if (provider == null) {
			if (other.provider != null)
				return false;
		} else if (!provider.equals(other.provider))
			return false;
		if (region == null) {
			if (other.region != null)
				return false;
		} else if (!region.equals(other.region))
			return false;
		return true;
	}

	public String asString(String delimiter) {
		return getProvider().getId() + delimiter + getRegion().getId() + delimiter + getHardwareType().getId();
	}
}
