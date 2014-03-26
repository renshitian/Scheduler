import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class scheduler {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		String data = "";
		String[] tokens;

		if(args.length==0){
			System.out.println("Please type in the Scheduler Type");
			System.exit(0);
		}
		
		int schedulertype = Integer.parseInt(args[0]);

		// get the data
		try {
			FileReader fr = new FileReader("input.txt");
			BufferedReader br = new BufferedReader(fr);

			String da;
			while ((da = br.readLine()) != null) {
				data += da;
				data += " ";
			}
			br.close();
			fr.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		// split data into tokens
		data = data.trim();
		tokens = data.split("\\s+");

		// store each process
		int processCount = 0;
		int count = 0;
		int[] processID = new int[20];
		int[] CPUTime = new int[20];
		int[] IOTime = new int[20];
		int[] arrivalTime = new int[20];
		while (count < tokens.length) {
			for (int i = 0; i < 4; i++) {
				if (i == 0) {
					processID[processCount] = Integer.parseInt(tokens[count]);
				} else if (i == 1) {
					CPUTime[processCount] = Integer.parseInt(tokens[count]);
				} else if (i == 2) {
					IOTime[processCount] = Integer.parseInt(tokens[count]);
				} else if (i == 3) {
					arrivalTime[processCount] = Integer.parseInt(tokens[count]);
					processCount++;
				}
				count++;
			}
		}

		int[] cpuProcessState = new int[processCount];
		for (int i = 0; i < processCount; i++) {
			cpuProcessState[i] = 1;
		}
		if (schedulertype == 0) {
			FCFS(processCount, processID, CPUTime, cpuProcessState, IOTime,
					arrivalTime);
		} else if (schedulertype == 1) {
			RR(processCount, processID, CPUTime, cpuProcessState, IOTime,
					arrivalTime);
		} else if (schedulertype == 2) {
			SRJF(processCount, processID, CPUTime, cpuProcessState, IOTime,
					arrivalTime);
		}
	}// end main method

	// First Come First Serve
	private static void FCFS(int processCount, int[] processID, int[] CPUTime,
			int[] cpuProcessState, int[] IOTime, int[] arrivalTime) {
		Queue<Integer> readyQueue = new LinkedList<Integer>();
		int servedProcessCount = 0;
		int timeCount = 0;
		int currentProcessNo = -1;
		Integer[] processPrintState = new Integer[processCount];

		int finishTime = 0;
		int CPUrunningcycle = 0;

		int[] cpuProcessingLeftTime = new int[2];
		HashMap<Integer, Integer> IOProcessing = new HashMap<Integer, Integer>();
		boolean[] tempAdding2Queue;
		boolean chooseNext = false;
		Integer[] processFinishTime = new Integer[processCount];
		String content2File = "FCFS\r\n";
		while (true) {
			for (int i = 0; i < processCount; i++) {
				processPrintState[i] = -1;// -1 ==not arrival or finished ;0 ready; 1 running, 2 blocked 
			}
			chooseNext = false;
			tempAdding2Queue = new boolean[processCount];
			// check IO time, and add process to queue when it finishes its IO time
			ArrayList<Integer> temp_IORemove = new ArrayList<Integer>();
			for (Integer key : IOProcessing.keySet()) {
				if (IOProcessing.get(key) == 0) {
					tempAdding2Queue[key] = true;
					temp_IORemove.add(key);
				}
			}

			for (Integer key : temp_IORemove) {
				IOProcessing.remove(key);
			}

			//check arrival process
			for (int i = 0; i < processCount; i++) {
				if (arrivalTime[i] == timeCount) {
					tempAdding2Queue[i] = true;
				}
			}

			if (currentProcessNo != (-1)) {
				if (cpuProcessingLeftTime[0] == 1
						&& cpuProcessingLeftTime[1] == 0) {
					//current running process goes into IO processing
					if (IOTime[currentProcessNo] > 0) {
						IOProcessing.put(currentProcessNo,
								IOTime[currentProcessNo]);
						// currentProcessNo's process state change to 2
						cpuProcessState[currentProcessNo] = 2;
						// set currentProcessNo = -1
						currentProcessNo = -1;
						cpuProcessingLeftTime[0] = -1;
						cpuProcessingLeftTime[1] = -1;
						// choose next process to serve
						chooseNext = true;

					} else if (IOTime[currentProcessNo] == 0) {
						//if this process has no IO time
						cpuProcessState[currentProcessNo] = 2;
						cpuProcessingLeftTime[1] = (CPUTime[currentProcessNo] / 2);
						cpuProcessingLeftTime[0] = cpuProcessState[currentProcessNo];
						chooseNext = false;
					}

				} else if (cpuProcessingLeftTime[0] == 2
						&& cpuProcessingLeftTime[1] == 0) {
					// currentProcessNo finish serve
					servedProcessCount++;
					processFinishTime[currentProcessNo] = timeCount - 1;
					if (servedProcessCount == processCount) {
						finishTime = timeCount - 1;
						break;
					}
					currentProcessNo = -1;
					cpuProcessingLeftTime[0] = -1;
					cpuProcessingLeftTime[1] = -1;
					chooseNext = true;
				}
			} else if (currentProcessNo == -1) {
				// reset current process
				cpuProcessingLeftTime[0] = -1;
				cpuProcessingLeftTime[1] = -1;

				// choose next

				chooseNext = true;
			}

			//do adding to queue
			for (int i = 0; i < processCount; i++) {
				if (tempAdding2Queue[i]) {
					readyQueue.add(i);
				}
			}

			if (chooseNext) {
				Integer temp = readyQueue.poll();
				if (temp != null) {
					//set current process and its state
					currentProcessNo = temp;
					cpuProcessingLeftTime[1] = (CPUTime[currentProcessNo] / 2);
					cpuProcessingLeftTime[0] = cpuProcessState[currentProcessNo];
				}
			}

			//record information for this time
			if (timeCount < 10) {
				content2File += (timeCount + ":   ");
				System.out.print(timeCount + ":   ");
			} else {
				System.out.print(timeCount + ":  ");
				content2File += (timeCount + ":  ");
			}
			if (currentProcessNo != -1) {
				CPUrunningcycle++;
				processPrintState[currentProcessNo] = 1;
			}
			if (!readyQueue.isEmpty()) {
				Integer[] temp = readyQueue.toArray(new Integer[0]);
				for (Integer key : temp) {
					processPrintState[key] = 0;
				}
			}
			if (!IOProcessing.isEmpty()) {
				for (Integer key : IOProcessing.keySet()) {
					processPrintState[key] = 2;
				}
			}

			for (int i = 0; i < processCount; i++) {
				if (processPrintState[i] == 0) {
					content2File = content2File + " " + i + " ready   ";
					System.out.print(" " + i + " ready   ");
				} else if (processPrintState[i] == 1) {
					content2File = content2File + " " + i + " running ";
					System.out.print(" " + i + " running ");
				} else if (processPrintState[i] == 2) {
					content2File = content2File + " " + i + " blocked ";
					System.out.print(" " + i + " blocked ");
				}

			}
			content2File = content2File + "\r\n";
			System.out.print("\n");
			//increas time count
			timeCount++;

			// IO time --
			for (int t : IOProcessing.keySet()) {
				int temp = IOProcessing.get(t) - 1;
				IOProcessing.put(t, temp);
			}
			// CPU time for current process served --
			if (currentProcessNo != (-1))
				cpuProcessingLeftTime[1]--;

		}

		content2File += ("Finish time " + finishTime + "\r\n");
		System.out.println("finish time " + finishTime);
		NumberFormat formatter = new DecimalFormat("#0.00");
		double cpuutilization = (double) (CPUrunningcycle)
				/ ((double) (finishTime + 1));
		content2File += ("CPU utilization: " + formatter.format(cpuutilization) + "\r\n");
		System.out.println("cpu utilization: "
				+ formatter.format(cpuutilization));
		for (int i = 0; i < processCount; i++) {
			content2File += ("Turnaround process " + i + ":  "
					+ (processFinishTime[i] - arrivalTime[i] + 1) + "\r\n");
			System.out.println("Turnaround process " + i + ":  "
					+ (processFinishTime[i] - arrivalTime[i] + 1));
		}
		filewriter(content2File,0);
	}// end First Come First Serve

	// Round Robin with Quantum 2
	private static void RR(int processCount, int[] processID, int[] CPUTime,
			int[] cpuProcessState, int[] IOTime, int[] arrivalTime) {

		System.out.println("RR");
		Queue<Integer> readyQueue = new LinkedList<Integer>();
		int servedProcessCount = 0;
		int timeCount = 0;
		int currentProcessNo = -1;
		int[] currentCPUProcessingLeftTime = new int[2];// state, left time
		HashMap<Integer, Integer[]> CPUProcissingLeftTime = new HashMap<Integer, Integer[]>();// processNo,
																								// (state,
																								// left
																								// time)
		HashMap<Integer, Integer> IOProcessing = new HashMap<Integer, Integer>();// processNo,
																					// left
																					// time
		for (int i = 0; i < processCount; i++) {
			CPUProcissingLeftTime.put(i, new Integer[] { 1, (CPUTime[i] / 2) });
		}

		boolean chooseNext = false;
		boolean[] tempAdding2Queue = new boolean[processCount];
		Integer[] processFinishTime = new Integer[processCount];
		int CPUrunningcycle = 0;
		Integer[] processPrintState = new Integer[processCount];
		int quantum = 0;
		String content2File = "RR\r\n";
		int finishTime = 0;

		while (true) {

			for (int i = 0; i < processCount; i++) {
				processPrintState[i] = -1;
			}

			chooseNext = false;
			tempAdding2Queue = new boolean[processCount];

			// check process finishing IO
			ArrayList<Integer> temp_IORemove = new ArrayList<Integer>();
			for (Integer key : IOProcessing.keySet()) {
				if (IOProcessing.get(key) == 0) {
					tempAdding2Queue[key] = true;
					temp_IORemove.add(key);
				}
			}

			for (Integer key : temp_IORemove) {
				IOProcessing.remove(key);
			}

			//check arrival process
			for (int i = 0; i < processCount; i++) {
				if (arrivalTime[i] == timeCount) {
					tempAdding2Queue[i] = true;
				}
			}

			if (currentProcessNo == (-1)) {
				currentCPUProcessingLeftTime[0] = -1;
				currentCPUProcessingLeftTime[1] = -1;
				chooseNext = true;

			} else if (currentProcessNo != (-1)) {
				//finish first part of CPU processing
				if (currentCPUProcessingLeftTime[0] == 1
						&& currentCPUProcessingLeftTime[1] == 0) {
					CPUProcissingLeftTime.put(currentProcessNo, new Integer[] {
							2, (CPUTime[currentProcessNo] / 2) });
					//if this process has IO time
					if (IOTime[currentProcessNo] > 0) {
						IOProcessing.put(currentProcessNo,
								IOTime[currentProcessNo]);
						chooseNext = true;
					} else if (IOTime[currentProcessNo] == 0) {
						//if this process has no IO time
						if (quantum == 2) {
							//if this process's quantum is 2
							chooseNext = true;
							tempAdding2Queue[currentProcessNo] = true;
						} else if (quantum < 2) {
							chooseNext = false;
						}
					}
					currentProcessNo = -1;
					currentCPUProcessingLeftTime[0] = -1;
					currentCPUProcessingLeftTime[1] = -1;
				}
				//if this process finishes all its work
				else if (currentCPUProcessingLeftTime[0] == 2
						&& currentCPUProcessingLeftTime[1] == 0) {
					servedProcessCount++;
					processFinishTime[currentProcessNo] = timeCount - 1;

					if (servedProcessCount == processCount) {
						finishTime = timeCount - 1;
						break;
					}
					currentProcessNo = -1;
					currentCPUProcessingLeftTime[0] = -1;
					currentCPUProcessingLeftTime[1] = -1;
					chooseNext = true;
				}
				else {
					//if this process's quantum is 2
					if (quantum == 2) {
						CPUProcissingLeftTime.put(currentProcessNo,
								new Integer[] {
										currentCPUProcessingLeftTime[0],
										currentCPUProcessingLeftTime[1] });
						tempAdding2Queue[currentProcessNo] = true;
						currentProcessNo = -1;
						currentCPUProcessingLeftTime[0] = -1;
						currentCPUProcessingLeftTime[1] = -1;

						chooseNext = true;

					}

					else if (quantum < 2) {
						//do nothing
					}
				}
			}
			//add to queue
			for (int i = 0; i < processCount; i++) {
				if (tempAdding2Queue[i]) {
					readyQueue.add(i);
				}
			}

			//choose next from queue
			if (chooseNext) {
				Integer nextReadyProcess = readyQueue.poll();
				if (nextReadyProcess != null) {

					currentProcessNo = nextReadyProcess;
					quantum = 0;
					currentCPUProcessingLeftTime[0] = CPUProcissingLeftTime
							.get(nextReadyProcess)[0];
					currentCPUProcessingLeftTime[1] = CPUProcissingLeftTime
							.get(nextReadyProcess)[1];
				}
			}

			if (timeCount < 10) {

				content2File = content2File + timeCount + ":   ";
				System.out.print(timeCount + ":   ");
			} else {
				content2File = content2File + timeCount + ":  ";
				System.out.print(timeCount + ":  ");
			}
			if (currentProcessNo != -1) {
				processPrintState[currentProcessNo] = 1;
				CPUrunningcycle++;
			}
			if (!readyQueue.isEmpty()) {
				Integer[] temp = readyQueue.toArray(new Integer[0]);
				for (Integer key : temp) {
					processPrintState[key] = 0;
				}
			}
			if (!IOProcessing.isEmpty()) {
				for (Integer key : IOProcessing.keySet()) {
					processPrintState[key] = 2;
				}
			}
			for (int i = 0; i < processCount; i++) {
				if (processPrintState[i] == 0) {
					content2File = content2File + " " + i + " ready   ";
					System.out.print(" " + i + " ready   ");
				} else if (processPrintState[i] == 1) {
					content2File = content2File + " " + i + " running ";
					System.out.print(" " + i + " running ");
				} else if (processPrintState[i] == 2) {
					content2File = content2File + " " + i + " blocked ";
					System.out.print(" " + i + " blocked ");
				}

			}
			content2File = content2File + "\r\n";
			System.out.print("\r\n");

			timeCount++;
			quantum++;
			// IO time --
			for (int t : IOProcessing.keySet()) {
				int temp = IOProcessing.get(t) - 1;
				IOProcessing.put(t, temp);
			}
			// currentProcess CPUtime --
			if (currentProcessNo != (-1))
				currentCPUProcessingLeftTime[1]--;
		}
		content2File += ("Finish time " + finishTime + "\r\n");
		System.out.println("finish time " + finishTime);
		NumberFormat formatter = new DecimalFormat("#0.00");
		double cpuutilization = (double) (CPUrunningcycle)
				/ ((double) (finishTime + 1));
		content2File += ("CPU utilization: " + formatter.format(cpuutilization) + "\r\n");
		System.out.println("cpu utilization: "
				+ formatter.format(cpuutilization));
		for (int i = 0; i < processCount; i++) {
			content2File += ("Turnaround process " + i + ":  "
					+ (processFinishTime[i] - arrivalTime[i] + 1) + "\r\n");
			System.out.println("Turnaround process " + i + ":  "
					+ (processFinishTime[i] - arrivalTime[i] + 1));
		}
		filewriter(content2File,1);
	}// end Round Robin

	private static void SRJF(int processCount, int[] processID, int[] CPUTime,
			int[] cpuProcessState, int[] IOTime, int[] arrivalTime) {

		System.out.println("SRJF");
		int servedProcessCount = 0;
		int timeCount = 0;
		int currentProcessNo = -1;
		Integer[] currentCPUProcessingLeftTime = new Integer[2];// 0-state;1-left-time
		boolean[] tempAdding2Queue = new boolean[processCount];
		Integer[] processFinishTime = new Integer[processCount];

		HashMap<Integer, Integer> IOProcessing = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer[]> CPUProcissingLeftTime = new HashMap<Integer, Integer[]>();// processNo,
																								// (state,left,
																								// time)
		Integer[] processPrintState = new Integer[processCount];

		for (int i = 0; i < processCount; i++) {
			CPUProcissingLeftTime.put(i, new Integer[] { 1, (CPUTime[i] / 2) });
		}
		Integer[][] RRPCPUTime = new Integer[2][processCount];
		for (int i = 0; i < processCount; i++) {
			RRPCPUTime[0][i] = -1;
			RRPCPUTime[1][i] = CPUTime[i];
		}
		int CPUrunningcycle = 0;
		int finishTime = 0;
		String content2File = "SRJF:\r\n";
		while (true) {
			for (int i = 0; i < processCount; i++) {
				processPrintState[i] = -1;
			}
			// check IO time finishing
			ArrayList<Integer> temp_IORemove = new ArrayList<Integer>();
			for (Integer key : IOProcessing.keySet()) {
				if (IOProcessing.get(key) == 0) {
					tempAdding2Queue[key] = true;
					RRPCPUTime[0][key] = 1;
					temp_IORemove.add(key);
				}
			}

			for (Integer key : temp_IORemove) {
				IOProcessing.remove(key);
			}

			// check arrival process
			for (int i = 0; i < processCount; i++) {
				if (arrivalTime[i] == timeCount) {
					RRPCPUTime[0][i] = 1;
					tempAdding2Queue[i] = true;
				}
			}

			if (currentProcessNo != -1) {
				//if this process finish first part of processing
				if (currentCPUProcessingLeftTime[0] == 1
						&& currentCPUProcessingLeftTime[1] == 0) {
					//if this process has IO time
					if (IOTime[currentProcessNo] > 0) {
						IOProcessing.put(currentProcessNo,
								IOTime[currentProcessNo]);
						RRPCPUTime[0][currentProcessNo] = -1;
						CPUProcissingLeftTime.put(currentProcessNo,
								new Integer[] { 2,
										(CPUTime[currentProcessNo] / 2) });
						
					} else if (IOTime[currentProcessNo] == 0) {
						//if this process has no IO time
						tempAdding2Queue[currentProcessNo] = true;
						RRPCPUTime[0][currentProcessNo] = 1;
						CPUProcissingLeftTime.put(currentProcessNo,
								new Integer[] { 2,
										(CPUTime[currentProcessNo] / 2) });
					}
				} else if (currentCPUProcessingLeftTime[0] == 2
						&& currentCPUProcessingLeftTime[1] == 0) {
					//if this process finishes all work
					RRPCPUTime[0][currentProcessNo] = -1;
					servedProcessCount++;
					processFinishTime[currentProcessNo] = timeCount - 1;
					if (servedProcessCount == processCount) {
						finishTime = timeCount - 1;
						break;
					}
				}
			} else if (currentProcessNo == -1) {
				// do nothing
			}
			currentProcessNo = -1;
			currentCPUProcessingLeftTime[0] = -1;
			currentCPUProcessingLeftTime[1] = -1;

			int tempMinCPUT = 100;
			int tempChooseResult = -1;

			
			//iterate all process finding the shortest job process
			for (int i = 0; i < processCount; i++) {
				if (RRPCPUTime[0][i] == 1) {
					if (RRPCPUTime[1][i] < tempMinCPUT) {
						tempMinCPUT = RRPCPUTime[1][i];
						tempChooseResult = i;
					}
				}
			}

			//set current process
			if (tempChooseResult != -1) {
				currentProcessNo = tempChooseResult;
				currentCPUProcessingLeftTime[0] = CPUProcissingLeftTime
						.get(currentProcessNo)[0];
				currentCPUProcessingLeftTime[1] = CPUProcissingLeftTime
						.get(currentProcessNo)[1];
			}

			if (timeCount < 10) {
				content2File = content2File + timeCount + ":   ";
				System.out.print(timeCount + ":   ");
			} else {
				content2File = content2File + timeCount + ":  ";
				System.out.print(timeCount + ":  ");
			}
			if (currentProcessNo != -1) {
				CPUrunningcycle++;
				processPrintState[currentProcessNo] = 1;
			}
			for (int i = 0; i < processCount; i++) {
				if (RRPCPUTime[0][i] == 1 && i != currentProcessNo) {
					processPrintState[i] = 0;
				}
			}
			if (!IOProcessing.isEmpty()) {
				for (Integer key : IOProcessing.keySet()) {
					processPrintState[key] = 2;
				}
			}
			for (int i = 0; i < processCount; i++) {
				if (processPrintState[i] == 0) {
					content2File = content2File + " " + i + " ready   ";
					System.out.print(" " + i + " ready   ");
				} else if (processPrintState[i] == 1) {
					content2File = content2File + " " + i + " running ";
					System.out.print(" " + i + " running ");
				} else if (processPrintState[i] == 2) {
					content2File = content2File + " " + i + " blocked ";
					System.out.print(" " + i + " blocked ");
				}

			}
			content2File = content2File + "\r\n";
			System.out.print("\r\n");
			timeCount++;
			if (currentProcessNo != -1) {
				RRPCPUTime[1][currentProcessNo]--;
				currentCPUProcessingLeftTime[1]--;
				CPUProcissingLeftTime.put(currentProcessNo, new Integer[] {
						currentCPUProcessingLeftTime[0],
						currentCPUProcessingLeftTime[1] });
			}

			for (int t : IOProcessing.keySet()) {
				int temp = IOProcessing.get(t) - 1;
				IOProcessing.put(t, temp);
			}
		}

		content2File += ("Finish time " + finishTime + "\r\n");
		System.out.println("finish time " + finishTime);
		NumberFormat formatter = new DecimalFormat("#0.00");
		double cpuutilization = (double) (CPUrunningcycle)
				/ ((double) (finishTime + 1));
		content2File += ("CPU utilization: " + formatter.format(cpuutilization) + "\r\n");
		System.out.println("cpu utilization: "
				+ formatter.format(cpuutilization));
		for (int i = 0; i < processCount; i++) {
			content2File += ("Turnaround process " + i + ":  "
					+ (processFinishTime[i] - arrivalTime[i] + 1) + "\r\n");
			System.out.println("Turnaround process " + i + ":  "
					+ (processFinishTime[i] - arrivalTime[i] + 1));
		}
		filewriter(content2File,2);
	}// end SRJF

	public static void filewriter(String str,int no) {
		File fl = new File("output"+"-"+no+".txt");
		FileWriter fw;
		try {
			fw = new FileWriter(fl);
			fw.write(str);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}// end class
