/*
 * Title:        CloudSim Toolkit
 * Description:  CloudSim (Cloud Simulation) Toolkit for Modeling and Simulation
 *               of Clouds
 * Licence:      GPL - http://www.gnu.org/copyleft/gpl.html
 *
 * Copyright (c) 2009, The University of Melbourne, Australia
 */


package research;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerSpaceShared;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * An example showing how to create
 * scalable simulations.
 */
public class HGA {

	/** The cloudlet list. */
	private static List<Cloudlet> cloudletList;

	/** The vmlist. */
	private static List<Vm> vmlist;

	private static List<Vm> createVM(int userId, int vms) {
		// Creates a container to store VMs. This list is passed to the broker
		// later
		LinkedList<Vm> list = new LinkedList<Vm>();
		ArrayList<Integer> randomSeed = getRandomSeed(vms, "VmRandomSeed");

		// VM Parameters
		long size = 10000; // image size (MB)
		int ram = 512; // vm memory (MB)
		int mips = 500;
		long bw = 10;
		int pesNumber = 4;// number of cpus
		String vmm = "Xen"; // VMM name
		Random rOb = new Random();
		// create VMs
		Vm[] vm = new Vm[vms];
		for (int i = 0; i < vms; i++) {
			long newMips = randomSeed.get(i) + mips;
			vm[i] = new Vm(i, userId, newMips, pesNumber, ram, bw, size, vmm,
					new CloudletSchedulerSpaceShared());
			// for creating a VM with a space shared scheduling policy for
			// cloudlets:
			// vm[i] = Vm(i, userId, mips, pesNumber, ram, bw, size, priority,
			// vmm, new CloudletSchedulerSpaceShared());
			list.add(vm[i]);
		}

		return list;
	}
	
	private static ArrayList<Integer> getRandomSeed(Integer constraint, String fileName){
		ArrayList<Integer> seed = new ArrayList<Integer>();
		File fobj = new File(System.getProperty("user.dir") + "/" + fileName);
		
		try {
			Integer count = constraint;
			Scanner sobj = new Scanner(fobj);
			while(sobj.hasNextLine() && count > 0) {
				seed.add(sobj.nextInt());
				count --;
			}
			
		} catch(FileNotFoundException e) {
			e.printStackTrace();;
		}
		return seed;
	}

	private static List<Cloudlet> createCloudlet(int userId, int cloudlets) {
		// Creates a container to store Cloudlets
		LinkedList<Cloudlet> list = new LinkedList<Cloudlet>();
		ArrayList<Integer> randomSeed = getRandomSeed(cloudlets, "RandomSeed");
		
		// cloudlet parameters
		long length = 1000;
		long fileSize = 300;
		long outputSize = 300;
		int pesNumber = 1;
		UtilizationModel utilizationModel = new UtilizationModelFull();

		Cloudlet[] cloudlet = new Cloudlet[cloudlets];
		
		for (int i = 0; i < cloudlets; i++) {
			long newLength = length + randomSeed.get(i);
			cloudlet[i] = new Cloudlet(i, newLength, pesNumber, fileSize, outputSize, utilizationModel,
					utilizationModel, utilizationModel);
			// setting the owner of these Cloudlets
			cloudlet[i].setUserId(userId);
			list.add(cloudlet[i]);
		}

		return list;
	}

	////////////////////////// STATIC METHODS ///////////////////////

	/**
	 * Creates main() to run this example
	 */
	
	/**
	 * 
	 *  HGA Written by Onwuka Daniel
	 *  https://github.com/OnwukaDaniel
	 *  
	 * @param args
	 */
	public static void main(String[] args) {
		Log.printLine("Starting CloudSimExample6...");

		try {
			// First step: Initialize the CloudSim package. It should be called
			// before creating any entities.
			int num_user = 1;   // number of grid users
			Calendar calendar = Calendar.getInstance();
			boolean trace_flag = false;  // mean trace events

			// Initialize the CloudSim library
			CloudSim.init(num_user, calendar, trace_flag);

			// Second step: Create Datacenters
			//Datacenters are the resource providers in CloudSim. We need at list one of them to run a CloudSim simulation
			@SuppressWarnings("unused")
			Datacenter datacenter0 = createDatacenter("Datacenter_0");
			@SuppressWarnings("unused")
			Datacenter datacenter1 = createDatacenter("Datacenter_1");

			//Third step: Create Broker
			DBGA broker = createBroker();
			int brokerId = broker.getId();

			//Fourth step: Create VMs and Cloudlets and send them to broker
			vmlist = createVM(brokerId, 30); // creating 20 vms
			cloudletList = createCloudlet(brokerId, 300); // creating 40 cloudlets

			int aveCls = (cloudletList.size() / vmlist.size());
			int rem = cloudletList.size() % vmlist.size();
			int noVms = vmlist.size();
			// Assign Cloudlets to VM
			List<List<List<Gene2>>> population = new ArrayList<>();
			List<Integer> fittnessList = new ArrayList<>();
			
			for(int x = 0; x < 100; x++) {
				List<List<Gene2>> chromosome = new ArrayList<>();
				for(int j = 0; j < 20; j++) {
					Collections.shuffle(vmlist);
					Collections.shuffle(cloudletList);
					int oldPosition = 0;
					List<Gene2> genelist = new ArrayList<>();
					for(int i = 0; i < noVms; i++) {
						if(i == noVms - 1 && rem != 0) { // FIlling the remainder Cloudlets if any exist.
							Gene2 geneLast = new Gene2(cloudletList.subList(oldPosition, cloudletList.size()), vmlist.get(i));
							genelist.add(geneLast);
							break;
						}
						List<Cloudlet> clts = cloudletList.subList(oldPosition, (i + 1) * aveCls);
						Gene2 gene = new Gene2(clts, vmlist.get(i));
						oldPosition = (i + 1) * aveCls;
						if(i < noVms) {
							genelist.add(gene);
						}
					}
					chromosome.add(genelist);
				}
				population.add(chromosome);
			}
			
			
			Boolean converged = false;
			List<Integer> convValues = new ArrayList<>();
			convValues.add(average(fitnessPopulation(population)));
			List<List<List<Gene2>>> best = population;
			
			while(converged == false) {
				List<List<List<Gene2>>> srt = sortPopulation(population);
				List<List<List<Gene2>>> newGeneration = new ArrayList<>();
				
				// ELITISM: 10% of the best
				newGeneration = srt.subList(0, (10 * srt.size()/100));
				List<List<List<Gene2>>> parent1 = new ArrayList<>();
				List<List<List<Gene2>>> parent2 = new ArrayList<>();
				//CROSS_OVER or MUTATION
				parent1.addAll(srt.subList(0, srt.size()));
				parent2.addAll(srt.subList(0, srt.size()));
				List<List<List<Gene2>>> p = mate(parent1, parent2, cloudletList, vmlist);
				newGeneration.addAll(p);
				population = newGeneration;
				int avg = average(fitnessPopulation(population));
				if(avg < Collections.min(convValues)) {
					best = newGeneration;
				}
				Collections.sort(convValues);
				if(convValues.size() > 500 || avg < Collections.min(convValues )) {
					converged = true;
				}
				if(convValues.size() > 500) {
					converged = true;
				}
				convValues.add(avg);
			}
			List<List<Gene2>> bestChromosome = pickBestChromosome(best);

			List<List<Gene2>> bestGeneList = population.get(0); // INITIALISED
			converged = false;
			while(converged == false) {
				List<List<Gene2>> srt = sortChromosome(bestChromosome);
				List<List<Gene2>> newGeneration = new ArrayList<>();
				
				// ELITISM: 10% of the best
				newGeneration = srt.subList(0, (10 * srt.size()/100));
				List<List<Gene2>> parent1 = new ArrayList<>();
				List<List<Gene2>> parent2 = new ArrayList<>();
				//CROSS_OVER or MUTATION
				parent1.addAll(srt.subList(0, srt.size()));
				parent2.addAll(srt.subList(0, srt.size()));
				List<List<Gene2>> p = mateChromosomes(parent1, parent2, cloudletList, vmlist);
				newGeneration.addAll(p);
				int avg = fitnessChromosome(newGeneration);
				if(avg < Collections.min(convValues)) {
					bestGeneList = newGeneration;
				}
				Collections.sort(convValues);
				if(convValues.size() > 500 || avg < Collections.min(convValues )) {
					converged = true;
				}
				if(convValues.size() > 500) {
					converged = true;
				}
				convValues.add(avg);
			}

			List<Gene2> bestGeneListList = pickBestGenelist(bestGeneList);
			List<Cloudlet> clts = new ArrayList<>();
			for(Gene2 gene: bestGeneListList) {
				for(int i = 0; i < gene.getCloudletsFromGene().size(); i++) {
					Log.printLine("getCloudletsFromGene ************************" + i);
					Cloudlet clt = gene.getCloudletsFromGene().get(i);
					clts.add(clt);
					clt.setUserId(brokerId);
					clt.setVmId(gene.getVmFromGene().getId());
					//broker.bindCloudletToVm(clt.getCloudletId(), gene.getVmFromGene().getId());
				}
			}
			broker.submitCloudletList(clts);
			broker.submitVmList(vmlist);

			// Fifth stecp: Starts the simulation
			CloudSim.startSimulation();

			// Final step: Print results when simulation is over
			List<Cloudlet> newList = broker.getCloudletReceivedList();

			CloudSim.stopSimulation();

			printCloudletList(newList);

			Log.printLine("CloudSimExample6 finished!");
		}
		catch (Exception e)
		{
			e.printStackTrace();
			Log.printLine("The simulation has been terminated due to an unexpected error");
		}
	}

	static Integer average(List<Integer> input) {
		int sum = 0;
		for(int num: input) { sum += num;}
		return sum / input.size();
	}

	static List<List<List<Gene2>>> sortPopulation(List<List<List<Gene2>>> population){
		List<List<Gene2>> temp= new ArrayList<>();
		for(int i = 0; i< population.size(); i++)
		{
		    for(int j = 0; j< population.size()-1; j++)
		    {
		        if(fitnessChromosome(population.get(j+1)) < fitnessChromosome(population.get(j)))
		        {
		            temp = population.get(j+1);
		            population.set(j+1, population.get(j));
		            population.set(j, temp);
		        }
		    }
		}
		return population;
	}

	static List<List<Gene2>> sortChromosome(List<List<Gene2>> chromosome){
		List<Gene2> temp= new ArrayList<>();
		for(int i = 0; i< chromosome.size(); i++)
		{
		    for(int j = 0; j< chromosome.size()-1; j++)
		    {
		        if(fitnessChromosome2(chromosome.get(j+1)) < fitnessChromosome2(chromosome.get(j)))
		        {
		            temp = chromosome.get(j+1);
		            chromosome.set(j+1, chromosome.get(j));
		            chromosome.set(j, temp);
		        }
		    }
		}
		return chromosome;
	}
	
	static List<List<Gene2>> pickBestChromosome(List<List<List<Gene2>>> population){
		List<List<Gene2>> best= new ArrayList<>();
		int bestValue = Integer.MAX_VALUE;
		for(int i = 0; i< population.size(); i++)
		{
			int fitValue = fitnessChromosome(population.get(i));
			//Log.printLine("fitValue ****************************************" + fitValue);
		    if(fitValue < bestValue)
		    {
		        best = population.get(i);
		        bestValue = fitnessChromosome(population.get(i));
		    }
		}
		return best;
	}
	
	static List<Gene2> pickBestGenelist(List<List<Gene2>> chromosome){
		List<Gene2> best= new ArrayList<>();
		int bestValue = Integer.MAX_VALUE;
		for(int i = 0; i< chromosome.size(); i++)
		{
			int fitValue = fitnessChromosome2(chromosome.get(i));
		    if(fitValue < bestValue)
		    {
		        best = chromosome.get(i);
		        bestValue = fitnessChromosome2(chromosome.get(i));
		    }
		}
		return best;
	}

	static List<List<List<Gene2>>> mate(List<List<List<Gene2>>> parent1, List<List<List<Gene2>>> parent2, List<Cloudlet> sortedList, List<Vm> sortedListVm){
		List<List<List<Gene2>>> children = new ArrayList<>();
		for(int x = 0; x < (90 * parent1.size()/100); x++) {
			Random rand = new Random();
			Double randNum = rand.nextDouble(1);
			if(randNum < 0.45) {
				children.add(parent1.get(x));
			} else if(randNum <= 1.0) {
				children.add(parent2.get(x));
			}
		}
		return children;
	}

	static List<List<Gene2>> mateChromosomes(List<List<Gene2>> parent1, List<List<Gene2>> parent2, List<Cloudlet> sortedList, List<Vm> sortedListVm){
		List<List<Gene2>> children = new ArrayList<>();
		for(int x = 0; x < (90 * parent1.size()/100); x++) {
			Random rand = new Random();
			Double randNum = rand.nextDouble(1);
			if(randNum < 0.45) {
				children.add(parent1.get(x));
			} else if(randNum <= 1.0) {
				children.add(parent2.get(x));
			}
		}
		return children;
	}
	
	static List<Integer> fitnessPopulation(List<List<List<Gene2>>> population) {
		int fitness = 0;
		List<Integer> fitnessList = new ArrayList<>(); 
		for(List<List<Gene2>> chromo : population) {
			fitness = fitnessChromosome(chromo);
			fitnessList.add(fitness);
		}
		 return fitnessList;
	}
	
	static int fitnessChromosome(List<List<Gene2>> chromo) {
		int fitness = 0; 
		for (List<Gene2> cr1: chromo) {
			for (Gene2 gene: cr1) { 
				fitness +=  fitnessGene(gene);
			}
		}
		return fitness;
	}
	
	static int fitnessChromosome2(List<Gene2> chromo) {
		int fitness = 0; 
		for (Gene2 geneList: chromo) {
			fitness +=  fitnessGene(geneList);
		}
		return fitness;
	}


	static int fitnessGene(Gene2 gene) {
		int fitness = 0;
		Vm vm = gene.getVmFromGene();
		List<Cloudlet> clt = gene.getCloudletsFromGene();
		for (Cloudlet ct: clt) {
			fitness += ct.getCloudletLength() / vm.getMips();
		}
		return fitness;
	}
	
	private static Datacenter createDatacenter(String name){

		// Here are the steps needed to create a PowerDatacenter:
		// 1. We need to create a list to store one or more
		//    Machines
		List<Host> hostList = new ArrayList<Host>();

		// 2. A Machine contains one or more PEs or CPUs/Cores. Therefore, should
		//    create a list to store these PEs before creating
		//    a Machine.
		List<Pe> peList1 = new ArrayList<Pe>();

		int mips = 10000;

		// 3. Create PEs and add these into the list.
		//for a quad-core machine, a list of 4 PEs is required:
		peList1.add(new Pe(0, new PeProvisionerSimple(mips))); // need to store Pe id and MIPS Rating
		peList1.add(new Pe(1, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(2, new PeProvisionerSimple(mips)));
		peList1.add(new Pe(3, new PeProvisionerSimple(mips)));

		//Another list, for a dual-core machine
		List<Pe> peList2 = new ArrayList<Pe>();

		peList2.add(new Pe(0, new PeProvisionerSimple(mips)));
		peList2.add(new Pe(1, new PeProvisionerSimple(mips)));

		//4. Create Hosts with its id and list of PEs and add them to the list of machines
		int hostId=0;
		int ram = 2048; //host memory (MB)
		long storage = 1000000; //host storage
		int bw = 10000;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList1,
    				new VmSchedulerTimeShared(peList1)
    			)
    		); // This is our first machine

		hostId++;

		hostList.add(
    			new Host(
    				hostId,
    				new RamProvisionerSimple(ram),
    				new BwProvisionerSimple(bw),
    				storage,
    				peList2,
    				new VmSchedulerTimeShared(peList2)
    			)
    		); // Second machine


		//To create a host with a space-shared allocation policy for PEs to VMs:
		//hostList.add(
    	//		new Host(
    	//			hostId,
    	//			new CpuProvisionerSimple(peList1),
    	//			new RamProvisionerSimple(ram),
    	//			new BwProvisionerSimple(bw),
    	//			storage,
    	//			new VmSchedulerSpaceShared(peList1)
    	//		)
    	//	);

		//To create a host with a oportunistic space-shared allocation policy for PEs to VMs:
		//hostList.add(
    	//		new Host(
    	//			hostId,
    	//			new CpuProvisionerSimple(peList1),
    	//			new RamProvisionerSimple(ram),
    	//			new BwProvisionerSimple(bw),
    	//			storage,
    	//			new VmSchedulerOportunisticSpaceShared(peList1)
    	//		)
    	//	);


		// 5. Create a DatacenterCharacteristics object that stores the
		//    properties of a data center: architecture, OS, list of
		//    Machines, allocation policy: time- or space-shared, time zone
		//    and its price (G$/Pe time unit).
		String arch = "x86";      // system architecture
		String os = "Linux";          // operating system
		String vmm = "Xen";
		double time_zone = 10.0;         // time zone this resource located
		double cost = 3.0;              // the cost of using processing in this resource
		double costPerMem = 0.05;		// the cost of using memory in this resource
		double costPerStorage = 0.1;	// the cost of using storage in this resource
		double costPerBw = 0.1;			// the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>();	//we are not adding SAN devices by now

		DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw);


		// 6. Finally, we need to create a PowerDatacenter object.
		Datacenter datacenter = null;
		try {
			datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return datacenter;
	}

	//We strongly encourage users to develop their own broker policies, to submit vms and cloudlets according
	//to the specific rules of the simulated scenario
	private static DBGA createBroker(){

		DBGA broker = null;
		try {
			broker = new DBGA("Broker");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return broker;
	}

	/**
	 * Prints the Cloudlet objects
	 * @param list  list of Cloudlets
	 */
	private static void printCloudletList(List<Cloudlet> list) {
		int size = list.size();
		Cloudlet cloudlet;
		Double cost = 0.0;

		String indent = "    ";
		Log.printLine();
		Log.printLine("========== OUTPUT ==========");
		Log.printLine("Cloudlet ID" + indent + "STATUS" + indent +
				"Data center ID" + indent + "VM ID" + indent + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

		DecimalFormat dft = new DecimalFormat("###.##");
		for (int i = 0; i < size; i++) {
			cloudlet = list.get(i);
			Log.print(indent + cloudlet.getCloudletId() + indent + indent);

			if (cloudlet.getCloudletStatus() == Cloudlet.SUCCESS){
				Log.print("SUCCESS");

				Log.printLine( indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getVmId() +
						indent + indent + indent + dft.format(cloudlet.getActualCPUTime()) +
						indent + indent + dft.format(cloudlet.getExecStartTime())+ indent + indent + indent + dft.format(cloudlet.getFinishTime()));
			}
			cost += (cloudlet.getCostPerSec() * cloudlet.getProcessingCost());
		}
	}
}
