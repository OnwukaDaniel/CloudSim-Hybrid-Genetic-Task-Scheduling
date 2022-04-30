
package research;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;
import java.util.ArrayList;

/**
 * @author deeksha
 *
 */
public class Chromosomes {

	protected ArrayList<Gene> geneList;

	public Chromosomes(ArrayList<Gene> geneList) {
		this.geneList = geneList;
	}

	public ArrayList<Gene> getGeneList() {
		return this.geneList;
	}

	public void updateGene(int index, Vm vm) {
		Gene gene = this.geneList.get(index);
		gene.setVmForGene(vm);
		this.geneList.set(index, gene);
	}

	public void updateGene(int index, Cloudlet cloudlet) {
		Gene gene = this.geneList.get(index);
		gene.setCloudletForGene(cloudlet);
		this.geneList.set(index, gene);
	}
}
