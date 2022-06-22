package research;

import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Vm;

/**
 * @author IO Daniel
 *
 */
public class Gene2 {

	private List<Cloudlet> tasks;
	private Vm vm;

	public Gene2(List<Cloudlet> cls, Vm v) {
		this.tasks = cls;
		this.vm = v;
	}

	public List<Cloudlet> getCloudletsFromGene() {
		return this.tasks;
	}

	public Vm getVmFromGene() {
		return this.vm;
	}

	public void setCloudletsForGene(List<Cloudlet> cls) {
		this.tasks = cls;
	}

	public void setVmForGene(Vm vm) {
		this.vm = vm;
	}

}
