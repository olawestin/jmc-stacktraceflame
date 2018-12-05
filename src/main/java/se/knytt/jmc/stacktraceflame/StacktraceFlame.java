package se.knytt.jmc.stacktraceflame;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.openjdk.jmc.common.IMCFrame;
import org.openjdk.jmc.common.IMCMethod;
import org.openjdk.jmc.common.item.IItemCollection;
import org.openjdk.jmc.common.util.FormatToolkit;
import org.openjdk.jmc.flightrecorder.CouldNotLoadRecordingException;
import org.openjdk.jmc.flightrecorder.JfrLoaderToolkit;
import org.openjdk.jmc.flightrecorder.jdk.JdkFilters;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator;
import org.openjdk.jmc.flightrecorder.stacktrace.FrameSeparator.FrameCategorization;
import org.openjdk.jmc.flightrecorder.stacktrace.StacktraceFrame;
import org.openjdk.jmc.flightrecorder.stacktrace.StacktraceModel;
import org.openjdk.jmc.flightrecorder.stacktrace.StacktraceModel.Branch;
import org.openjdk.jmc.flightrecorder.stacktrace.StacktraceModel.Fork;

public class StacktraceFlame {

	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.err.println("No input file specified");
			System.exit(1);
		}
		File jfrFile = new File(args[0]);
		PrintStream out = System.out;
		if (args.length > 1) {
			out = new PrintStream(new File(args[1]));
		}

		printFlameData(jfrFile, out);
	}

	private static void printFlameData(File file, PrintStream out) throws IOException, CouldNotLoadRecordingException {
		IItemCollection items = JfrLoaderToolkit.loadEvents(file);
		// Filter as desired
		IItemCollection executionSampleItems = items.apply(JdkFilters.EXECUTION_SAMPLE);
		StacktraceModel sModel = new StacktraceModel(true, new FrameSeparator(FrameCategorization.METHOD, false),
				executionSampleItems);
		Fork root = sModel.getRootFork();
		printFlameData(root, "", out);
	}

	private static void printFlameData(Fork fork, String parentFrameNames, PrintStream out) {
		for (Branch branch : fork.getBranches()) {
			StacktraceFrame countedFrame = branch.getFirstFrame();
			int itemCount = countedFrame.getItemCount();
			String branchFrameNames = parentFrameNames + format(branch.getFirstFrame());
			for (StacktraceFrame tailFrame : branch.getTailFrames()) {
				// Look for non-branching leafs
				if (tailFrame.getItemCount() < itemCount) {
					out.print(branchFrameNames + " " + (itemCount - tailFrame.getItemCount()) + "\n");
					countedFrame = tailFrame;
					itemCount = tailFrame.getItemCount();
				}
				branchFrameNames = branchFrameNames + ";" + format(tailFrame);
			}
			Fork endFork = branch.getEndFork();
			if (itemCount - endFork.getItemsInFork() > 0) {
				// No need to print forking frame if it is not a leaf
				out.print(branchFrameNames + " " + (itemCount - endFork.getItemsInFork()) + "\n");
			}
			printFlameData(endFork, branchFrameNames + ";", out);
		}
	}

	private static String format(StacktraceFrame sFrame) {
		IMCFrame frame = sFrame.getFrame();
		IMCMethod method = frame.getMethod();
		// Either use the default method formatting
		return FormatToolkit.getHumanReadable(method);
		// Or use some custom formatting
//		return method.getType().getFullName() + "." + method.getMethodName();
	}
}
