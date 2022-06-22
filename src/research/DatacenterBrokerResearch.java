/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009-2012, The University of Melbourne, Australia
 */

package research;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.lists.CloudletList;
import org.cloudbus.cloudsim.lists.VmList;


/**
 * DatacentreBroker represents a broker acting on behalf of a user. It hides VM management, as vm
 * creation, sumbission of cloudlets to this VMs and destruction of VMs.
 * 
 * @author Rodrigo N. Calheiros
 * @author Anton Beloglazov
 * @since CloudSim Toolkit 1.0
 */
public class DatacenterBrokerResearch extends SimEntity {

	/** The vm list. */
	protected List<? extends Vm> vmList;

	/** The vms created list. */
	protected List<? extends Vm> vmsCreatedList;

	/** The cloudlet list. */
	protected List<? extends Cloudlet> cloudletList;

	/** The cloudlet submitted list. */
	protected List<? extends Cloudlet> cloudletSubmittedList;

	/** The cloudlet received list. */
	protected List<? extends Cloudlet> cloudletReceivedList;

	/** The cloudlets submitted. */
	protected int cloudletsSubmitted;

	/** The vms requested. */
	protected int vmsRequested;

	/** The vms acks. */
	protected int vmsAcks;

	/** The vms destroyed. */
	protected int vmsDestroyed;

	/** The datacenter ids list. */
	protected List<Integer> datacenterIdsList;

	/** The datacenter requested ids list. */
	protected List<Integer> datacenterRequestedIdsList;

	/** The vms to datacenters map. */
	protected Map<Integer, Integer> vmsToDatacentersMap;

	/** The datacenter characteristics list. */
	protected Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList;

	/**
	 * Created a new DatacenterBroker object.
	 * 
	 * @param name name to be associated with this entity (as required by Sim_entity class from
	 *            simjava package)
	 * @throws Exception the exception
	 * @pre name != null
	 * @post $none
	 */
	public DatacenterBrokerResearch(String name) throws Exception {
		super(name);

		setVmList(new ArrayList<Vm>());
		setVmsCreatedList(new ArrayList<Vm>());
		setCloudletList(new ArrayList<Cloudlet>());
		setCloudletSubmittedList(new ArrayList<Cloudlet>());
		setCloudletReceivedList(new ArrayList<Cloudlet>());

		cloudletsSubmitted = 0;
		setVmsRequested(0);
		setVmsAcks(0);
		setVmsDestroyed(0);

		setDatacenterIdsList(new LinkedList<Integer>());
		setDatacenterRequestedIdsList(new ArrayList<Integer>());
		setVmsToDatacentersMap(new HashMap<Integer, Integer>());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());
	}

	/**
	 * This method is used to send to the broker the list with virtual machines that must be
	 * created.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitVmList(List<? extends Vm> list) {
		getVmList().addAll(list);
	}

	/**
	 * This method is used to send to the broker the list of cloudlets.
	 * 
	 * @param list the list
	 * @pre list !=null
	 * @post $none
	 */
	public void submitCloudletList(List<? extends Cloudlet> list) {
		getCloudletList().addAll(list);
	}

	/**
	 * Specifies that a given cloudlet must run in a specific virtual machine.
	 * 
	 * @param cloudletId ID of the cloudlet being bount to a vm
	 * @param vmId the vm id
	 * @pre cloudletId > 0
	 * @pre id > 0
	 * @post $none
	 */
	public void bindCloudletToVm(int cloudletId, int vmId) {
		CloudletList.getById(getCloudletList(), cloudletId).setVmId(vmId);
	}

	/**
	 * Processes events available for this Broker.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	@Override
	public void processEvent(SimEvent ev) {
		switch (ev.getTag()) {
		// Resource characteristics request
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;
			// Resource characteristics answer
			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			// VM Creation answer
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			// A finished cloudlet returned
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			// if the simulation finishes
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
			// other unknown tags are processed by this method
			default:
				processOtherEvent(ev);
				break;
		}
	}

	/**
	 * Process the return of a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristics(SimEvent ev) {
		DatacenterCharacteristics characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			setDatacenterRequestedIdsList(new ArrayList<Integer>());
			createVmsInDatacenter(getDatacenterIdsList().get(0));
		}
	}

	/**
	 * Process a request for the characteristics of a PowerDatacenter.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processResourceCharacteristicsRequest(SimEvent ev) {
		setDatacenterIdsList(CloudSim.getCloudResourceList());
		setDatacenterCharacteristicsList(new HashMap<Integer, DatacenterCharacteristics>());

		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloud Resource List received with "
				+ getDatacenterIdsList().size() + " resource(s)");

		for (Integer datacenterId : getDatacenterIdsList()) {
			sendNow(datacenterId, CloudSimTags.RESOURCE_CHARACTERISTICS, getId());
		}
	}

	/**
	 * Process the ack received due to a request for VM creation.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		if (result == CloudSimTags.TRUE) {
			getVmsToDatacentersMap().put(vmId, datacenterId);
			getVmsCreatedList().add(VmList.getById(getVmList(), vmId));
			Log.printLine(CloudSim.clock() + ": " + getName() + ": VM #" + vmId
					+ " has been created in Datacenter #" + datacenterId + ", Host #"
					+ VmList.getById(getVmsCreatedList(), vmId).getHost().getId());
		} else {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Creation of VM #" + vmId
					+ " failed in Datacenter #" + datacenterId);
		}

		incrementVmsAcks();

		// all the requested VMs have been created
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			// all the acks received, but some VMs were not created
			if (getVmsRequested() == getVmsAcks()) {
				// find id of the next datacenter that has not been tried
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				// all datacenters already queried
				if (getVmsCreatedList().size() > 0) { // if some vm were created
					submitCloudlets();
				} else { // no vms created. abort
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}

	/**
	 * Process a cloudlet return event.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != $null
	 * @post $none
	 */
	protected void processCloudletReturn(SimEvent ev) {
		Cloudlet cloudlet = (Cloudlet) ev.getData();
		getCloudletReceivedList().add(cloudlet);
		Log.printLine(CloudSim.clock() + ": " + getName() + ": Cloudlet " + cloudlet.getCloudletId()
				+ " received");
		cloudletsSubmitted--;
		if (getCloudletList().size() == 0 && cloudletsSubmitted == 0) { // all cloudlets executed
			Log.printLine(CloudSim.clock() + ": " + getName() + ": All Cloudlets executed. Finishing...");
			clearDatacenters();
			finishExecution();
		} else { // some cloudlets haven't finished yet
			if (getCloudletList().size() > 0 && cloudletsSubmitted == 0) {
				// all the cloudlets sent finished. It means that some bount
				// cloudlet is waiting its VM be created
				clearDatacenters();
				createVmsInDatacenter(0);
			}

		}
	}

	/**
	 * Overrides this method when making a new and different type of Broker. This method is called
	 * by {@link #body()} for incoming unknown tags.
	 * 
	 * @param ev a SimEvent object
	 * @pre ev != null
	 * @post $none
	 */
	protected void processOtherEvent(SimEvent ev) {
		if (ev == null) {
			Log.printLine(getName() + ".processOtherEvent(): " + "Error - an event is null.");
			return;
		}

		Log.printLine(getName() + ".processOtherEvent(): "
				+ "Error - event unknown by this DatacenterBroker.");
	}

	/**
	 * Create the virtual machines in a datacenter.
	 * 
	 * @param datacenterId Id of the chosen PowerDatacenter
	 * @pre $none
	 * @post $none
	 */
	protected void createVmsInDatacenter(int datacenterId) {
		// send as much vms as possible for this datacenter before trying the next one
		int requestedVms = 0;
		String datacenterName = CloudSim.getEntityName(datacenterId);
		for (Vm vm : getVmList()) {
			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": Trying to Create VM #" + vm.getId()
						+ " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		getDatacenterRequestedIdsList().add(datacenterId);

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	}

	/**
	 * Submit cloudlets to the created VMs.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void submitCloudlets() {
		int vmIndex = 0;

		List<Vm> vmList = getVmsCreatedList();
		List<Cloudlet> cloudletsList = getCloudletList();
	
		CustomPair geneticPair = geneticAlgorithm(getCloudletList(), getVmsCreatedList());
		cloudletsList = geneticPair.getFirst();
		vmList = geneticPair.getSecond();
	
		for (Cloudlet cloudlet : cloudletsList) {
			Vm vm;
		// if user didn't bind this cloudlet and it has not been executed yet
			if (cloudlet.getVmId() == -1) {
				vm = vmList.get(vmIndex);
			} else { // submit to the specific vm
				vm = VmList.getById(vmList, cloudlet.getVmId());
				if (vm == null) { // vm was not created
					Log.printLine(CloudSim.clock() + ": " + getName() + ": Postponing execution of cloudlet "
							+ cloudlet.getCloudletId() + ": bount VM not available");
					continue;
				}
			}

			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			vmIndex = (vmIndex + 1) % vmList.size();
			getCloudletSubmittedList().add(cloudlet);
		}

	// remove submitted cloudlets from waiting list
		for (Cloudlet cloudlet : getCloudletSubmittedList()) {
			getCloudletList().remove(cloudlet);
		}
	}

	static CustomPair geneticAlgorithm(List<Cloudlet> cloudletList, List<Vm> vmlist) {
		// SORTING CLOUDLETS BY LENGTH
		List<Cloudlet> sortedList = new ArrayList<Cloudlet>();
		for (Cloudlet cloudlet : cloudletList) {
			sortedList.add(cloudlet);
		}
		int numCloudlets = sortedList.size();
		for (int i = 0; i < numCloudlets; i++) {
			Cloudlet tmp = sortedList.get(i);
			int idx = i;
			for (int j = i + 1; j < numCloudlets; j++) {
				if (sortedList.get(j).getCloudletLength() < tmp.getCloudletLength()) {
					idx = j;
					tmp = sortedList.get(j);
				}
			}
			Cloudlet tmp2 = sortedList.get(i);
			sortedList.set(i, tmp);
			sortedList.set(idx, tmp2);
		}

				
	// SORTING CLOUDLETS BY LENGTH
		ArrayList<Vm> sortedListVm = new ArrayList<Vm>();
		ArrayList<Vm> toBeUsedVm = new ArrayList<Vm>();
		ArrayList<Vm> leftOutVm = new ArrayList<Vm>();
		for (Vm vm : vmlist) {
			sortedListVm.add(vm);
		}
		int numVms = sortedListVm.size();
	
		for (int i = 0; i < numVms; i++) {
			Vm tmp = sortedListVm.get(i);
			int idx = i;
			if (i < numCloudlets)
				toBeUsedVm.add(tmp);
			else
				leftOutVm.add(tmp);
			for (int j = i + 1; j < numVms; j++) {
				if (sortedListVm.get(j).getMips() > tmp.getMips()) {
					idx = j;
					tmp = sortedListVm.get(j);
				}
			}
			Vm tmp2 = sortedListVm.get(i);
			sortedListVm.set(i, tmp);
			sortedListVm.set(idx, tmp2);
		}
		
		// BEGINNING OF CHROMOSOME ALGORITHM
	
		// INITIAL POPULATION AND FIRST-CHROMOSOME ARE CREATED.
		// K IS USED TO GET THE THE POSITION OF VM TO CREATE A GENE
		// I IS USED TO GET THE THE POSITION OF CLOUDLET TO CREATE A GENE
		// THE THE GENE IS ADDED TO A LIST OF GENES TO CREATE A CHROMOSOME AND THE CHROMOSOME TO A INITIAL-LIST OF CHROMOSOMES
	
		ArrayList<Chromosomes> initialPopulation = new ArrayList<Chromosomes>();
		for (int j = 0; j < numCloudlets * numVms; j++) {
			ArrayList<Gene> firstChromosome = new ArrayList<Gene>();
			
			for (int i = 0; i < numCloudlets; i++) { 
				int k = (i + j) % numVms;
				//k = (k + numCloudlets) % numCloudlets;
				Gene geneObj = new Gene(sortedList.get(i), sortedListVm.get(k));
				firstChromosome.add(geneObj);
			}
			Chromosomes chromosome = new Chromosomes(firstChromosome);
			initialPopulation.add(chromosome);
		}
	
		List<String> chromesPopulation = new ArrayList<String>();
		for(Chromosomes chromosome : initialPopulation) {
			String chromes = "";
			for(Gene gene : chromosome.getGeneList()) {
				//int txt = (int) (gene.getCloudletFromGene().getCloudletLength() / gene.getVmFromGene().getMips());
				chromes += "[" + "Task " + gene.getCloudletFromGene().getCloudletId() + ".  Vm " + gene.getVmFromGene().getId() + "]";
			}
			chromesPopulation.add(chromes);
			//Log.printLine("Chromes " + chromes);
		}
		
		int populationSize = initialPopulation.size();
		Random random = new Random();
		for (int itr = 0; itr < populationSize; itr++) {
			int index1, index2;
			index1 = random.nextInt(populationSize) % populationSize;
			index2 = random.nextInt(populationSize) % populationSize;
			ArrayList<Gene> l1 = new ArrayList<Gene>();
			l1 = initialPopulation.get(index1).getGeneList();
			Chromosomes chromosome1 = new Chromosomes(l1);
			ArrayList<Gene> l2 = new ArrayList<Gene>();
			l2 = initialPopulation.get(index2).getGeneList();
			Chromosomes chromosome2 = new Chromosomes(l2);
			double rangeMin = 0.0f;
			double rangeMax = 1.0f;
			Random r = new Random();
			double crossProb = rangeMin + (rangeMax - rangeMin) * r.nextDouble();
			if (crossProb < 0.5) {
				int i, j;
				i = random.nextInt(numCloudlets) % numCloudlets;
				j = random.nextInt(numCloudlets) % numCloudlets;
				Vm vm1 = l1.get(i).getVmFromGene();
				Vm vm2 = l2.get(j).getVmFromGene();
				chromosome1.updateGene(i, vm2);
				chromosome2.updateGene(j, vm1);
				initialPopulation.set(index1, chromosome1);
				initialPopulation.set(index2, chromosome2);
			}
		}
		
		// FITNESS
		int fittestIndex = 0;
		double time = 1000000;
		List<Integer> fitnessList = new ArrayList<Integer>();
		
		for (int i = 0; i < populationSize; i++) {
			ArrayList<Gene> geneList = new ArrayList<Gene>();
			geneList = initialPopulation.get(i).getGeneList();
			double sum = 0;
			for (int j = 0; j < numCloudlets; j++) {
				Gene gene = geneList.get(j);
				Cloudlet c = gene.getCloudletFromGene();
				Vm v = gene.getVmFromGene();
				double temp = (Double) Math.log10((int) c.getCloudletLength()) /v.getMips() ;
				sum += temp;
			}
			if (sum < time) {
				time = sum;
				fittestIndex = i;
				fitnessList.add(fittestIndex);
			}
		}
		
		chromesPopulation = new ArrayList<String>();
		String chromes = "";
		for(Gene gene : initialPopulation.get(fittestIndex).getGeneList()) {
		int txt = 0;
			txt = (int) (gene.getCloudletFromGene().getCloudletLength() / gene.getVmFromGene().getMips());
			chromes += "[" + "Task " + gene.getCloudletFromGene().getCloudletId() + ".  Vm " + gene.getVmFromGene().getId() + "=== MIPS " + txt + " ]";
		}
		chromesPopulation.add(chromes);
		Log.printLine("Chromes " + chromes);
		
		ArrayList<Gene> geneList = initialPopulation.get(fittestIndex).getGeneList();
		List<CustomDoublePair> cLst = listOfVmAndFitness(geneList);  // STORED THE GENE VM ID (first) AND ITS SUM OF FITNESS(second)
		
		// Find Gene with minimum fitness.
		Double m = 1000.0;
		Integer mmin = 0;
		for(int i = 0; i < cLst.size(); i++) {
			if(cLst.get(i).second < m) {
				m = cLst.get(i).second;
				mmin = i;
			}
		}
		CustomDoublePair minX = cLst.get(mmin); //Index of VM and it's fitness which is minimum
		
		// Get from geneList all genes corresponding to the lowest fitness from the VMID
		//By getting all genes that has the VMID compute the max fitness and
		//get its index in the geneList for further continuation
		List<CustomGenePair> listGeneMin = new ArrayList<CustomGenePair>();
		for(int i = 0; i < geneList.size(); i++) {
			if(geneList.get(i).getVmFromGene().getId() == minX.first) {
				Gene g = geneList.get(i);
				Integer in = i;
				CustomGenePair pp = new CustomGenePair(in, g);
				listGeneMin.add(pp); // All gene whose VM has lowest fitness
			}
		}
		
		// Find Gene with maximum fitness.
		Double mx = 1000.0;
		Integer mmax = 0;
		for(int i = 0; i < cLst.size(); i++) {
			if(cLst.get(i).second < mx) {
				mx = cLst.get(i).second;
				mmax = i;
			}
		}
		CustomDoublePair maxX = cLst.get(mmax); //Index of VM and it's fitness which is maximum
		
		// Get from geneList all genes corresponding to the maximum fitness from the VMID
		//By getting all genes that has the VMID compute the max fitness and
		//get its index in the geneList for further continuation
		List<CustomGenePair> listGeneMx = new ArrayList<CustomGenePair>();
		for(int i = 0; i < geneList.size(); i++) {
			if(geneList.get(i).getVmFromGene().getId() == maxX.first) {
				Gene g = geneList.get(i);
				Integer in = i;
				CustomGenePair pp = new CustomGenePair(in, g);
				listGeneMx.add(pp); // All gene whose VM has highest fitness
			}
		}

		// Find CloudLet with minimum length.
		Long minCV = Long.MAX_VALUE;
		Cloudlet mminC = listGeneMin.get(0).getSecond().getCloudletFromGene();
		for(int i = 0; i < listGeneMin.size(); i++) {
			Cloudlet clMM = listGeneMin.get(i).getSecond().getCloudletFromGene();
			if(clMM.getCloudletLength() < minCV) {
				minCV = clMM.getCloudletLength();
				mminC = clMM;
			}
		}
		// Find CloudLet with maximum length.
		Long mxCV = Long.MIN_VALUE;
		Cloudlet mmaxC = listGeneMx.get(0).getSecond().getCloudletFromGene();
		for(int i = 0; i < listGeneMx.size(); i++) {
			Cloudlet clMM = listGeneMx.get(i).getSecond().getCloudletFromGene();
			if(clMM.getCloudletLength() > mxCV) {
				mxCV = clMM.getCloudletLength();
				mmaxC = clMM;
			}
		}
		
		//If cloudLet length of gene containing VM with minimum fitness is lesser
		// than that containing maximum fitness
		
		// Find cloudLet and replace with min
		// Find cloudLet and replace with max
		Log.printLine("MIN VM ID ***************************** " + mminC.getCloudletLength());
		Log.printLine("MAX VM ID ***************************** " + mmaxC.getCloudletLength());
		if(mminC.getCloudletLength() < mmaxC.getCloudletLength()) {
			for(int i = 0; i < geneList.size(); i++) {
				Log.printLine("1 ***************************** " + geneList.get(i).getCloudletFromGene().getCloudletLength());
				Log.printLine("2 ***************************** " + geneList.get(i).getVmFromGene().getMips());
				if (mminC.getCloudletId() == geneList.get(i).getCloudletFromGene().getCloudletId()) {
					initialPopulation.get(fittestIndex).updateGene(i, mmaxC);
				}
				if (mmaxC.getCloudletId() == geneList.get(i).getCloudletFromGene().getCloudletId()) {
					//Log.printLine("2 ***************************** " + mmaxC.getCloudletLength());
					initialPopulation.get(fittestIndex).updateGene(i, mminC);
				}
			}
		}

		//CustomDoublePair min = min(initialPopulation.get(fittestIndex).getGeneList());
		//CustomDoublePair max = max(initialPopulation.get(fittestIndex).getGeneList());

		//Gene gene = initialPopulation.get(fittestIndex).getGeneList().get(min.first);
		//Gene geneMax = initialPopulation.get(fittestIndex).getGeneList().get(max.first);

		//gene.setVmForGene(geneMax.getVmFromGene());
		//geneMax.setVmForGene(gene.getVmFromGene());

		//initialPopulation.get(fittestIndex).updateGene(min.first, geneMax.getVmFromGene());
		//initialPopulation.get(fittestIndex).updateGene(max.first, gene.getVmFromGene());
		
		//Log.printLine("MIN VM ID ***************************** " + gene.getVmFromGene().getId());
		//Log.printLine("MIN VM MIPS ***************************** " + gene.getVmFromGene().getMips());

		//Log.printLine("MAX VM ID ***************************** " + geneMax.getVmFromGene().getId());
		//Log.printLine("MAX VM MIPS ***************************** " + geneMax.getVmFromGene().getMips());

		ArrayList<Gene> result = new ArrayList<Gene>();
		result = initialPopulation.get(fittestIndex).getGeneList();
		
		List<Cloudlet> finalcloudletList = new ArrayList<Cloudlet>();
		List<Vm> finalvmlist = new ArrayList<Vm>();
		
		for (int i = 0; i < result.size(); i++) {
			finalcloudletList.add(result.get(i).getCloudletFromGene());
			finalvmlist.add(result.get(i).getVmFromGene());
		}
		CustomPair pairOfResources = new CustomPair(finalcloudletList, finalvmlist);
		return pairOfResources;
	}
	
	static List<CustomDoublePair> listOfVmAndFitness(ArrayList<Gene> geneList) {
		List<CustomDoublePair> list = new ArrayList<CustomDoublePair>(); // STORES THE GENE VM ID AND ITS SUM OF FITNESS
		for(int i = 0; i < geneList.size(); i++) {
			CustomDoublePair pair = new CustomDoublePair(0, 0.0);
			Double fitness = 0.0;
			for(int j = 0; j < geneList.size(); j++) {
				if(geneList.get(i).getVmFromGene().getId() == geneList.get(j).getVmFromGene().getId()) {
					fitness += geneList.get(j).getCloudletFromGene().getCloudletLength() / geneList.get(j).getVmFromGene().getMips();
				}
			}
			pair.setFirst(geneList.get(i).getVmFromGene().getId());
			pair.setSecond(fitness);
			list.add(pair);
		}
		return list;
	}
	
	static CustomDoublePair min(ArrayList<Gene> list) {
		Integer minIdx = 0;
		Double minValue = 0.0;
		Double minimumReg = 10000000.0;
		for(int i = 0; i < list.size(); i++) {
			Double txt = 0.0;
			Gene gene = list.get(i);
			txt = (gene.getCloudletFromGene().getCloudletLength() / gene.getVmFromGene().getMips());
			minValue = txt;
			if(minValue < minimumReg) {
				minimumReg = minValue;
				minIdx = i;
			}
		}
		return new CustomDoublePair(minIdx, minimumReg);
	}
	
	static CustomDoublePair max(ArrayList<Gene> list) {
		Integer maxIdx = 0;
		Double maxValue = 0.0;
		Double maxReg = 0.0;
		for(int i = 0; i < list.size(); i++) {
			Double txt = 0.0;
			Gene gene = list.get(i);
			txt = (gene.getCloudletFromGene().getCloudletLength() / gene.getVmFromGene().getMips());
			maxValue = txt;
			if(maxValue > maxReg) {
				maxReg = maxValue;
				maxIdx = i;
			}
		}
		return new CustomDoublePair(maxIdx, maxReg);
	}
	
	/**
	 * Destroy the virtual machines running in datacenters.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void clearDatacenters() {
		for (Vm vm : getVmsCreatedList()) {
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Destroying VM #" + vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.VM_DESTROY, vm);
		}

		getVmsCreatedList().clear();
	}

	/**
	 * Send an internal event communicating the end of the simulation.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void finishExecution() {
		sendNow(getId(), CloudSimTags.END_OF_SIMULATION);
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#shutdownEntity()
	 */
	@Override
	public void shutdownEntity() {
		Log.printLine(getName() + " is shutting down...");
	}

	/*
	 * (non-Javadoc)
	 * @see cloudsim.core.SimEntity#startEntity()
	 */
	@Override
	public void startEntity() {
		Log.printLine(getName() + " is starting...");
		schedule(getId(), 0, CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST);
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmList() {
		return (List<T>) vmList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmList the new vm list
	 */
	protected <T extends Vm> void setVmList(List<T> vmList) {
		this.vmList = vmList;
	}

	/**
	 * Gets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletList() {
		return (List<T>) cloudletList;
	}

	/**
	 * Sets the cloudlet list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletList the new cloudlet list
	 */
	protected <T extends Cloudlet> void setCloudletList(List<T> cloudletList) {
		this.cloudletList = cloudletList;
	}

	/**
	 * Gets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet submitted list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletSubmittedList() {
		return (List<T>) cloudletSubmittedList;
	}

	/**
	 * Sets the cloudlet submitted list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletSubmittedList the new cloudlet submitted list
	 */
	protected <T extends Cloudlet> void setCloudletSubmittedList(List<T> cloudletSubmittedList) {
		this.cloudletSubmittedList = cloudletSubmittedList;
	}

	/**
	 * Gets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @return the cloudlet received list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Cloudlet> List<T> getCloudletReceivedList() {
		return (List<T>) cloudletReceivedList;
	}

	/**
	 * Sets the cloudlet received list.
	 * 
	 * @param <T> the generic type
	 * @param cloudletReceivedList the new cloudlet received list
	 */
	protected <T extends Cloudlet> void setCloudletReceivedList(List<T> cloudletReceivedList) {
		this.cloudletReceivedList = cloudletReceivedList;
	}

	/**
	 * Gets the vm list.
	 * 
	 * @param <T> the generic type
	 * @return the vm list
	 */
	@SuppressWarnings("unchecked")
	public <T extends Vm> List<T> getVmsCreatedList() {
		return (List<T>) vmsCreatedList;
	}

	/**
	 * Sets the vm list.
	 * 
	 * @param <T> the generic type
	 * @param vmsCreatedList the vms created list
	 */
	protected <T extends Vm> void setVmsCreatedList(List<T> vmsCreatedList) {
		this.vmsCreatedList = vmsCreatedList;
	}

	/**
	 * Gets the vms requested.
	 * 
	 * @return the vms requested
	 */
	protected int getVmsRequested() {
		return vmsRequested;
	}

	/**
	 * Sets the vms requested.
	 * 
	 * @param vmsRequested the new vms requested
	 */
	protected void setVmsRequested(int vmsRequested) {
		this.vmsRequested = vmsRequested;
	}

	/**
	 * Gets the vms acks.
	 * 
	 * @return the vms acks
	 */
	protected int getVmsAcks() {
		return vmsAcks;
	}

	/**
	 * Sets the vms acks.
	 * 
	 * @param vmsAcks the new vms acks
	 */
	protected void setVmsAcks(int vmsAcks) {
		this.vmsAcks = vmsAcks;
	}

	/**
	 * Increment vms acks.
	 */
	protected void incrementVmsAcks() {
		vmsAcks++;
	}

	/**
	 * Gets the vms destroyed.
	 * 
	 * @return the vms destroyed
	 */
	protected int getVmsDestroyed() {
		return vmsDestroyed;
	}

	/**
	 * Sets the vms destroyed.
	 * 
	 * @param vmsDestroyed the new vms destroyed
	 */
	protected void setVmsDestroyed(int vmsDestroyed) {
		this.vmsDestroyed = vmsDestroyed;
	}

	/**
	 * Gets the datacenter ids list.
	 * 
	 * @return the datacenter ids list
	 */
	protected List<Integer> getDatacenterIdsList() {
		return datacenterIdsList;
	}

	/**
	 * Sets the datacenter ids list.
	 * 
	 * @param datacenterIdsList the new datacenter ids list
	 */
	protected void setDatacenterIdsList(List<Integer> datacenterIdsList) {
		this.datacenterIdsList = datacenterIdsList;
	}

	/**
	 * Gets the vms to datacenters map.
	 * 
	 * @return the vms to datacenters map
	 */
	protected Map<Integer, Integer> getVmsToDatacentersMap() {
		return vmsToDatacentersMap;
	}

	/**
	 * Sets the vms to datacenters map.
	 * 
	 * @param vmsToDatacentersMap the vms to datacenters map
	 */
	protected void setVmsToDatacentersMap(Map<Integer, Integer> vmsToDatacentersMap) {
		this.vmsToDatacentersMap = vmsToDatacentersMap;
	}

	/**
	 * Gets the datacenter characteristics list.
	 * 
	 * @return the datacenter characteristics list
	 */
	protected Map<Integer, DatacenterCharacteristics> getDatacenterCharacteristicsList() {
		return datacenterCharacteristicsList;
	}

	/**
	 * Sets the datacenter characteristics list.
	 * 
	 * @param datacenterCharacteristicsList the datacenter characteristics list
	 */
	protected void setDatacenterCharacteristicsList(
			Map<Integer, DatacenterCharacteristics> datacenterCharacteristicsList) {
		this.datacenterCharacteristicsList = datacenterCharacteristicsList;
	}

	/**
	 * Gets the datacenter requested ids list.
	 * 
	 * @return the datacenter requested ids list
	 */
	protected List<Integer> getDatacenterRequestedIdsList() {
		return datacenterRequestedIdsList;
	}

	/**
	 * Sets the datacenter requested ids list.
	 * 
	 * @param datacenterRequestedIdsList the new datacenter requested ids list
	 */
	protected void setDatacenterRequestedIdsList(List<Integer> datacenterRequestedIdsList) {
		this.datacenterRequestedIdsList = datacenterRequestedIdsList;
	}

}

