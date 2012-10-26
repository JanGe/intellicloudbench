package edu.kit.aifb.libIntelliCloudBench.stopping;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.aifb.libIntelliCloudBench.IService;
import edu.kit.aifb.libIntelliCloudBench.background.Runner;
import edu.kit.aifb.libIntelliCloudBench.model.Benchmark;
import edu.kit.aifb.libIntelliCloudBench.model.InstanceType;

public class StoppingConfiguration implements Serializable {
	private static final long serialVersionUID = -3361301504863667619L;

	public static final List<Class<? extends StoppingMethod>> STOPPING_METHODS = new ArrayList<>();
	private static final List<String> STOPPING_METHODS_NAME = new ArrayList<>();
	private static final List<Integer> STOPPING_METHODS_PARAM = new ArrayList<>();

	static {
		STOPPING_METHODS.add(NonStopper.class);
		STOPPING_METHODS_NAME.add("Do not stop");
		STOPPING_METHODS_PARAM.add(null);
		STOPPING_METHODS.add(OrderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Instance for Instance, 2-times-marked");
		STOPPING_METHODS_PARAM.add(2);
		STOPPING_METHODS.add(OrderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Instance for Instance, 3-times-marked");
		STOPPING_METHODS_PARAM.add(3);
		STOPPING_METHODS.add(OrderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Instance for Instance, 4-times-marked");
		STOPPING_METHODS_PARAM.add(4);
		STOPPING_METHODS.add(OrderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Instance for Instance, 5-times-marked");
		STOPPING_METHODS_PARAM.add(5);
		STOPPING_METHODS.add(OrderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Instance for Instance, 6-times-marked");
		STOPPING_METHODS_PARAM.add(6);
		STOPPING_METHODS.add(OrderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Instance for Instance, 7-times-marked");
		STOPPING_METHODS_PARAM.add(7);
		STOPPING_METHODS.add(UnorderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Instance for Instance, 2-times-marked");
		STOPPING_METHODS_PARAM.add(2);
		STOPPING_METHODS.add(UnorderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Instance for Instance, 3-times-marked");
		STOPPING_METHODS_PARAM.add(3);
		STOPPING_METHODS.add(UnorderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Instance for Instance, 4-times-marked");
		STOPPING_METHODS_PARAM.add(4);
		STOPPING_METHODS.add(UnorderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Instance for Instance, 5-times-marked");
		STOPPING_METHODS_PARAM.add(5);
		STOPPING_METHODS.add(UnorderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Instance for Instance, 6-times-marked");
		STOPPING_METHODS_PARAM.add(6);
		STOPPING_METHODS.add(UnorderedSequentialStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Instance for Instance, 7-times-marked");
		STOPPING_METHODS_PARAM.add(7);
		STOPPING_METHODS.add(OrderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Parallel, 0-shift-threshold");
		STOPPING_METHODS_PARAM.add(0);
		STOPPING_METHODS.add(OrderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Parallel, 0.25-shift-threshold");
		STOPPING_METHODS_PARAM.add(25);
		STOPPING_METHODS.add(OrderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Parallel, 0.5-shift-threshold");
		STOPPING_METHODS_PARAM.add(50);
		STOPPING_METHODS.add(OrderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Ordered Parallel, 0.75-shift-threshold");
		STOPPING_METHODS_PARAM.add(75);
		STOPPING_METHODS.add(UnorderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Parallel, 0-shift-threshold");
		STOPPING_METHODS_PARAM.add(0);
		STOPPING_METHODS.add(UnorderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Parallel, 0.25-shift-threshold");
		STOPPING_METHODS_PARAM.add(25);
		STOPPING_METHODS.add(UnorderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Parallel, 0.5-shift-threshold");
		STOPPING_METHODS_PARAM.add(50);
		STOPPING_METHODS.add(UnorderedParallelStopper.class);
		STOPPING_METHODS_NAME.add("Unordered Parallel, 0.75-shift-threshold");
		STOPPING_METHODS_PARAM.add(75);
		STOPPING_METHODS.add(EvaluationStopper.class);
		STOPPING_METHODS_NAME.add("Do not stop and evaluate all others");
		STOPPING_METHODS_PARAM.add(null);
	}

	private Integer selectedIndex = 0;

	public Integer getSelectedStoppingMethodIndex() {
		return selectedIndex;
	}

	public static Class<? extends StoppingMethod> getStoppingMethod(Integer index) {
		return STOPPING_METHODS.get(index);
	}

	public void setSelectedStoppingMethodIndex(Integer index) {
		this.selectedIndex = index;
	}

	public static String getName(Integer index) {
		return STOPPING_METHODS_NAME.get(index);
	}

	public static Integer getIndexOf(Class<? extends String> stoppingMethod) {
		return STOPPING_METHODS.indexOf(stoppingMethod);
	}

	public static StoppingMethod newInstanceOf(Integer index, IService service, Class<? extends Runner> runnerClass,
	    List<InstanceType> checkedInstanceTypes, List<Benchmark> benchmarks) throws InstantiationException,
	    IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
	    SecurityException {
		
		return STOPPING_METHODS.get(index)
		    .getDeclaredConstructor(IService.class, Class.class, List.class, List.class, Integer.class)
		    .newInstance(service, runnerClass, checkedInstanceTypes, benchmarks, STOPPING_METHODS_PARAM.get(index));
	}
}
