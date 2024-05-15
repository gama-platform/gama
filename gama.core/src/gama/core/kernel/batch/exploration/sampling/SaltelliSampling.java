package gama.core.kernel.batch.exploration.sampling;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.moeaframework.util.sequence.Saltelli;

import gama.core.kernel.experiment.ParametersSet;
import gama.core.kernel.experiment.IParameter.Batch;
import gama.core.runtime.IScope;

/**
 * 
 * @author tomroy && raphaeldupont
 *
 */

/**
 * 
 * This class make a Saltelli Sampling for a Sobol analysis
 *
 */
public class SaltelliSampling extends SamplingUtils{
	

	
	private static List<Map<String,Double>> transform_into_list(double[][] saltelli,List<String> names,int sample){
		List<Map<String,Double>> tmpList= new ArrayList<>();
		for(int i=0;i<sample;i++) {
			Map<String,Double> tmpMap=new LinkedHashMap<>();
			for(int y=0;y<names.size();y++) {
				tmpMap.put(names.get(y), saltelli[i][y]);
			}
			tmpList.add(tmpMap);
		}
		return tmpList;
		
	}
	private static List<Map<String,Double>> setRandomSaltelliSampling(int sample, List<Batch> parameters) {
		double[][] saltelli = new Saltelli().generate(sample, parameters.size());
        List<String> nameInputs= new ArrayList<>();
        for(int i=0;i<parameters.size();i++) {
        	nameInputs.add(parameters.get(i).getName());
        }
		List<Map<String,Double>>  Saltelli_sequence= transform_into_list(saltelli,nameInputs,sample);
		return Saltelli_sequence;
	}
	
	public static List<ParametersSet> makeSaltelliSampling(IScope scope,int sample,List<Batch> parameters){
		List<Map<String,Double>> saltelli= setRandomSaltelliSampling(sample,parameters);
        return buildParametersSetfromSample(scope,parameters,saltelli);
	}

}
