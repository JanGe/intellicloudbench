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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import org.jclouds.compute.ComputeServiceContext;
import org.jclouds.compute.domain.Hardware;
import org.jclouds.domain.Location;
import org.jclouds.providers.ProviderMetadata;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

public class Provider extends Observable implements Serializable {
	private static final long serialVersionUID = 7347235110052708192L;

	private String id;
	private String name;
	private Credentials credentials;
	private Collection<Region> allRegions = new TreeSet<Region>();
	private boolean loadedAllRegions = false;
	private Collection<HardwareType> allHardwareTypes = new TreeSet<HardwareType>();
	private boolean loadedAllHardwareTypes = false;

	private static Map<Provider, List<ICredentialsChangedListener>> credentialsListener =
	    new HashMap<Provider, List<ICredentialsChangedListener>>();

	private static ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());

	public Provider(ProviderMetadata provider) {
		this.id = provider.getId();
		this.name = provider.getName();
		this.credentials = new Credentials();
		credentialsListener.put(this, new ArrayList<ICredentialsChangedListener>(1));
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Credentials getCredentials() {
		return credentials;
	}

	public boolean areCredentialsSetup() {
		return (!credentials.getKey().equals("") || !credentials.getSecret().equals(""));
	}

	public void credentialsChanged() {
		synchronized (allRegions) {
			loadedAllRegions = false;
			allRegions.clear();
		}
		for (ICredentialsChangedListener listener : credentialsListener.get(this)) {
			listener.notifyCredentialsChanged(this, credentials);
		}
	}

	public void registerCredentialsChangedListener(ICredentialsChangedListener listener) {
		credentialsListener.get(this).add(listener);
	}

	public Iterable<Region> getAllRegions(ComputeServiceContext context, Observer observer) throws NotReadyException {
		if (allRegions.isEmpty()) {
			updateAllRegions(context, observer);
		}
		if (loadedAllRegions) {
			return allRegions;
		} else {
			throw new NotReadyException("allRegions");
		}
	}

	public Iterable<HardwareType> getAllHardwareTypes(ComputeServiceContext context, Observer observer)
	    throws NotReadyException {
		if (allHardwareTypes.isEmpty()) {
			updateAllHardwareTypes(context, observer);
		}
		if (loadedAllHardwareTypes) {
			return allHardwareTypes;
		} else {
			throw new NotReadyException("allHardwareTypes");
		}
	}

	public void updateAllRegions(final ComputeServiceContext context, final Observer observer) {
		ListenableFuture<Set<Location>> allInProgress = executor.submit(new Callable<Set<Location>>() {

			@Override
			public Set<Location> call() throws Exception {
				return Collections.unmodifiableSet(context.getComputeService().listAssignableLocations());
			}

		});
		Futures.addCallback(allInProgress, new FutureCallback<Set<Location>>() {

			@Override
			public void onSuccess(Set<Location> locations) {
				synchronized (allRegions) {
					loadedAllRegions = false;
					allRegions.clear();
					for (Location location : locations) {
						Region region = new Region(location);
						allRegions.add(region);
					}
					loadedAllRegions = true;
				}
				synchronized (observer) {
					observer.update(Provider.this, allRegions);
				}
			}

			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
				synchronized (observer) {
					observer.update(Provider.this, t);
				}
			}

		});
	}

	public void updateAllHardwareTypes(final ComputeServiceContext context, final Observer observer) {
		ListenableFuture<Set<Hardware>> allInProgress = executor.submit(new Callable<Set<Hardware>>() {

			@Override
			public Set<Hardware> call() throws Exception {
				return Collections.unmodifiableSet(context.getComputeService().listHardwareProfiles());
			}

		});
		Futures.addCallback(allInProgress, new FutureCallback<Set<Hardware>>() {

			@Override
			public void onSuccess(Set<Hardware> hardwareList) {
				synchronized (allHardwareTypes) {
					loadedAllHardwareTypes = false;
					allHardwareTypes.clear();
					for (Hardware hardware : hardwareList) {
						HardwareType hardwareType = new HardwareType(hardware);
						allHardwareTypes.add(hardwareType);
					}
					loadedAllHardwareTypes = true;
				}

				synchronized (observer) {
					observer.update(Provider.this, allHardwareTypes);
				}
			}

			@Override
			public void onFailure(Throwable t) {
				t.printStackTrace();
				synchronized (observer) {
					observer.update(Provider.this, t);
				}
			}

		});
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
		Provider other = (Provider) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
