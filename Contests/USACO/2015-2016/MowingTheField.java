import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.StringTokenizer;

public class MowingTheField {

	public static void main(String[] args) throws IOException {
		new MowingTheField().execute();
	}

	int tGap;

	void execute() throws IOException {

		BufferedReader reader = new BufferedReader(new FileReader("mowing.in"));
		StringTokenizer inputData = new StringTokenizer(reader.readLine());
		int numPos = Integer.parseInt(inputData.nextToken());
		tGap = Integer.parseInt(inputData.nextToken());

		int[] posX = new int[numPos];
		int[] posY = new int[numPos];

		for (int i = 0; i < numPos; i++) {
			inputData = new StringTokenizer(reader.readLine());
			posX[i] = Integer.parseInt(inputData.nextToken());
			posY[i] = Integer.parseInt(inputData.nextToken());
		}
		reader.close();

		long startTime = System.currentTimeMillis();
		int[] copy = new int[posY.length];
		System.arraycopy(posY, 0, copy, 0, posY.length);
		int[] clone = copy;
		Arrays.sort(clone);

		HashMap<Integer, Integer> yMap = new HashMap<Integer, Integer>();
		maxY = 1;
		for (int v : clone) {
			yMap.put(v, maxY++);
		}

		for (int i = 0; i < numPos; i++) {
			posY[i] = yMap.get(posY[i]);
		}

		System.out.println(System.currentTimeMillis() - startTime);

		ArrayList<HEvent> hEvents = new ArrayList<HEvent>();
		ArrayList<VEvent> vEvents = new ArrayList<VEvent>();

		for (int i = 0; i + 1 < numPos; i++) {
			int cT = i + 1;
			if (posX[i] == posX[i + 1]) {
				// vertical
				vEvents.add(new VEvent(posX[i], Math.min(posY[i], posY[i + 1]), Math.max(posY[i], posY[i + 1]), cT));
			} else {
				// horizontal
				if (posX[i] < posX[i + 1]) {
					hEvents.add(new HEvent(posX[i], posY[i], 1, cT));
					hEvents.add(new HEvent(posX[i + 1], posY[i], -1, cT));
				} else {
					hEvents.add(new HEvent(posX[i + 1], posY[i], 1, cT));
					hEvents.add(new HEvent(posX[i], posY[i], -1, cT));
				}
			}
		}

		Collections.sort(hEvents);
		Collections.sort(vEvents);

		maxY = 1_000_000_000;
		maxT = numPos - 1;
		BIT = new Node[2 * maxT];
		for (int i = 1; i < BIT.length; i++) {
			BIT[i] = new Node();
		}

		int nxtH = 0;

		int sum = 0;
		for (VEvent query : vEvents) {
			while (nxtH < hEvents.size() && (hEvents.get(nxtH).x < query.x
					|| (hEvents.get(nxtH).mod == -1 && hEvents.get(nxtH).x == query.x))) {
				HEvent cUpdate = hEvents.get(nxtH);
				update(cUpdate.t, cUpdate.y, cUpdate.mod);
				nxtH++;
			}
			sum += query(1, query.t - tGap, query.y1 + 1, query.y2 - 1);
			sum += query(query.t + tGap, maxT, query.y1 + 1, query.y2 - 1);
		}

		PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter("mowing.out")));
		printer.println(sum);
		printer.close();
		System.out.println(nodeCount);
	}

	class VEvent implements Comparable<VEvent> {
		int x;
		int y1;
		int y2;
		int t;

		VEvent(int x, int y1, int y2, int t) {
			this.x = x;
			this.y1 = y1;
			this.y2 = y2;
			this.t = t;
		}

		public int compareTo(VEvent o) {
			if (x != o.x) {
				return Integer.compare(x, o.x);
			}
			return Integer.compare(y1, o.y1);
		}
	}

	class HEvent implements Comparable<HEvent> {
		int x;
		int y;
		int mod;
		int t;

		HEvent(int x, int y, int mod, int t) {
			this.x = x;
			this.y = y;
			this.mod = mod;
			this.t = t;
		}

		public int compareTo(HEvent o) {
			if (x != o.x) {
				return Integer.compare(x, o.x);
			}
			if (mod != o.mod) {
				// -1 should come first
				return Integer.compare(mod, o.mod);
			}
			return Integer.compare(y, o.y);
		}
	}

	int nodeCount = 0;

	// an allocated on demand segment tree
	// memory efficient for sparse data sets
	class Node {

		Node() {
			nodeCount++;
		}

		int value = 0;
		Node left;
		Node right;

		void update(int target, int delta) {
			update(1, maxY, target, delta);
		}

		void update(int cL, int cR, int target, int delta) {
			value += delta;
			if (cL != cR) {
				int mid = (cL + cR) / 2;
				if (target <= mid) {
					if (left == null) {
						left = new Node();
					}
					left.update(cL, mid, target, delta);
				} else {
					if (right == null) {
						right = new Node();
					}
					right.update(mid + 1, cR, target, delta);
				}
			}
		}

		int query(int qL, int qR) {
			return query(1, maxY, qL, qR);
		}

		int query(int cL, int cR, int qL, int qR) {
			if (cR < qL || qR < cL) {
				return 0;
			}
			if (qL <= cL && cR <= qR) {
				return value;
			}
			int mid = (cL + cR) / 2;
			int sum = 0;
			if (left != null) {
				sum += left.query(cL, mid, qL, qR);
			}
			if (right != null) {
				sum += right.query(mid + 1, cR, qL, qR);
			}
			return sum;
		}
	}

	Node[] BIT;

	void update(int arg1, int arg2, int delta) {
		while (arg1 <= maxT) {
			BIT[arg1].update(arg2, delta);
			arg1 += (arg1 & -arg1);
		}
	}

	int query(int qL1, int qR1, int qL2, int qR2) {
		if (qL1 > qR1 || qL2 > qR2) {
			return 0;
		}

		int exclude = 0;
		qL1--;
		while (qL1 > 0) {
			exclude += BIT[qL1].query(qL2, qR2);
			qL1 -= (qL1 & -qL1);
		}

		int include = 0;
		while (qR1 > 0) {
			include += BIT[qR1].query(qL2, qR2);
			qR1 -= (qR1 & -qR1);
		}

		return include - exclude;
	}

	static int maxY;
	static int maxT;

}
