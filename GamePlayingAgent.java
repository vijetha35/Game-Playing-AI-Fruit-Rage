import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
public class GamePlayingAgent {
	static Double time_out_limit=0.2;
	static int nodesExpanded=0;
	static int iterativeDeep=0;
	static Double time_for_move=0.0;
	static boolean terminalReachedOrTimeOut = false;
	static int max_depth=0;
	static char [] columnAlpha = {'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	static boolean alreadyCalculatedFruits = false;
	static private long start_time;
	static private double time_limit;
	static private int num_fruits;
	static private int size;
	static private String bestStr;
	static private String non_star;
	static private Node gameStart;
	static private Character result_matrix[][];
	static Double nodes_per_second=0.0;
	public static class Node{
		private Character currentboard[][] ;
		private int depth;
		private double adversarial_score;
		private double our_score;
		private String parent_move;
		private String move;
		List<Entry<String, Double>> list;

		public void setCurrentboard(Character[][] currentboard) {
			this.currentboard = currentboard;
		}
		public void setSize(int Bsize) {
			size = Bsize;
		}
		public void setNum_fruits(int num_fruit_types) {
			num_fruits = num_fruit_types;
		}
		public void setTime_remaining(double time_lim) {
			time_limit = time_lim;
		}
		public Character[][] getCurrentboard() {
			return currentboard;
		}
		public int getSize() {
			return size;
		}
		public int getNum_fruits() {
			return num_fruits;
		}
		public int getDepth() {
			return depth;
		}
		public void setDepth(int depth) {
			this.depth = depth;
		}
		public double getAdversarial_score() {
			return adversarial_score;
		}
		public void setAdversarial_score(double adversarial_score) {
			this.adversarial_score = adversarial_score;
		}
		public double getOur_score() {
			return our_score;
		}
		public void setOur_score(double our_score) {
			this.our_score = our_score;
		}

		public Node(Character[][] currentboard, int Boardsize, int num_fruit_types, double time_lim) {

			this.currentboard = currentboard;
			size = Boardsize;
			num_fruits = num_fruit_types;
			time_limit = time_lim;
			start_time = System.nanoTime();
			this.our_score=0;
			this.adversarial_score=0;
			this.depth=0;
			this.parent_move="A1";
		}
		public Node(Character[][] currentboard) {

			this.currentboard = currentboard;
			this.our_score=0;
			this.adversarial_score=0;
			this.depth=0;
			
		}
		
		public Node(Integer n) {
			this.currentboard = new Character[size][size];
		}
		public String getMove() {
			return move;
		}
		public void setMove(String move) {
			this.move = move;
		}

	}

	public static double getTime_remaining() {
		return time_limit - 	(double)((System.nanoTime() -start_time)/1000000000 );
	}
	public static double getTimeRemainingForMove() {
		return time_for_move - 	(double)((System.nanoTime() -start_time)/1000000000) ;
	}
	public static void main(String[] args) {
		FileReader fr=null;
		Scanner scan=null;
		int width,num_fruits;
		double time_remaining ;
		String line;
		try {
			fr =new 	FileReader("input/game.txt");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		if(fr==null)
			return;
		scan = new Scanner(fr);
		width = Integer.parseInt(scan.nextLine());
		num_fruits = Integer.parseInt(scan.nextLine());
		time_remaining =  Float.parseFloat(scan.nextLine());

		Character game_matrix [][] = new Character[width][width];
		boolean non_star_found= false;
		for(int i=0;i<width;i++)
		{
			line = scan.nextLine();

			for(int j =0;j<width;j++)
			{
				if(line.charAt(j) != '*' && ! non_star_found)
				{
					non_star_found=true;
					non_star = columnAlpha[j] + "" + (i+1);
				}

				game_matrix[i][j]= line.charAt(j);
			}
		}
		try {
			fr =new 	FileReader("calibration.txt");

		} catch (FileNotFoundException e) {
			nodes_per_second=46845.34379189415;
		}
		catch(Exception e) {
			nodes_per_second =46845.34379189415;

		}
		scan = new Scanner(fr);
		nodes_per_second = Double.parseDouble(scan.nextLine());

		Node root = new Node(game_matrix,width,num_fruits,time_remaining);
		/*PrintStream out = null;
		try {
			out = new PrintStream(new FileOutputStream("output.txt"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.setOut(out);*/
		gameStart=root;
		double startTime ,endTime;
		startTime=System.nanoTime();
		int branching_factor=0;
		Double number_of_nodes=0.0;
		if(root.list == null )
		{
			findNextMovesWithScoresAndOrder(game_matrix, root);
			if(root.list!=null)
				branching_factor = root.list.size();

		}
		Node result;
		
		time_for_move=(time_limit/2);
		

		for(int depth=1;depth<=50 ; depth++ )
		{
			number_of_nodes =Math.pow(branching_factor,depth);
			if(number_of_nodes <= time_for_move * nodes_per_second)
			{
				max_depth =depth;
			}
			else 
				break;
		}


		if(time_for_move <0.1 && max_depth==0)
		{
			System.out.println(non_star);
			int column = non_star.charAt(0)-65;
			int row = Integer.parseInt(non_star.substring(1)) -1;
			boolean visited[][] = new boolean[size][size];
			markNeighbouringFruits(game_matrix, row, column, game_matrix[row][column], visited);
			applyGravity(game_matrix, size);
			print2D(game_matrix);

		}
		else if(max_depth==0 && time_for_move <0.5) 
		{

			result = generateNode(root.list.get(0).getKey(),root.list.get(0).getValue(), root, root.currentboard);
			System.out.println(result.move);
			print2D(result.getCurrentboard());
		}

		else if( time_for_move <0.5)
		{

			result = generateNode(root.list.get(0).getKey(),root.list.get(0).getValue(), root, root.currentboard);
			System.out.println(result.move);
			print2D(result.getCurrentboard());
		}

		else
		{


			iterativeDeep=max_depth;
			

			do
			{
				startTime = System.nanoTime();
				double alpha = Max(width,game_matrix,root,Double.NEGATIVE_INFINITY,Double.POSITIVE_INFINITY);
				endTime = System.nanoTime();
				iterativeDeep++;
				if(getTimeRemainingForMove() > ((endTime -startTime )/1000000000)) 
					break;
				else if(terminalReachedOrTimeOut )
				{
					break;
				}
			}while(iterativeDeep<=(max_depth+1));
			System.out.println(bestStr);
			print2D(result_matrix);

		}
	}
	private static double Max(int width, Character[][] game_matrix, Node root,double alpha , double beta) {
		if(root.list==null)
			findNextMovesWithScoresAndOrder(game_matrix,root);

		if(cutOff_test(root) || iterativeDeep== root.getDepth() || IsTimeOut()   ) 
		{
			return Evaluation(root);
		}

		double v = Double.NEGATIVE_INFINITY;
		double best;
	
		for(Map.Entry<String, Double> entry: root.list)
		{
			Node eachMove = generateNode(entry.getKey(), entry.getValue(), root,game_matrix);
		

			best =v;

			v= Math.max(v, Min(width,eachMove.currentboard, eachMove,alpha,beta));
			alpha =Math.max(alpha,v);
			if(v >= beta)
			{
				root.parent_move =  eachMove.move;
				if(root==gameStart)
				{
					bestStr = eachMove.move;
					result_matrix=eachMove.currentboard;
				}
				return v;
			}
			if(v>best )
			{
				root.parent_move = eachMove.move;
				if(root==gameStart)
				{
					bestStr = eachMove.move;
					result_matrix=eachMove.currentboard;
				}

			}


		}
		return v;
	}
	private static boolean IsTimeOut() {
		if( getTimeRemainingForMove() <=time_out_limit)
		{
			
			terminalReachedOrTimeOut=true;
			return true;
		}
		return false;
	}
	private static double Min(int width, Character[][] currentboard, Node root, double alpha, double beta) {
		if(root.list==null)
			findNextMovesWithScoresAndOrder(currentboard,root);

		if( cutOff_test(root)  || iterativeDeep== root.getDepth()   )
		{

			return Evaluation(root);

		}
		double v = Double.POSITIVE_INFINITY;
		double best;
		for(Map.Entry<String, Double> entry:root.list)
		{
			Node eachMove = generateNode(entry.getKey(), entry.getValue(), root,currentboard);
			best =v;
			v= Math.min(v, Max(width,eachMove.currentboard, eachMove,alpha,beta));
			beta =Math.min(beta,v);
			if(v <= alpha)
			{
				root.parent_move= eachMove.move;
				return v;
			}
			if(best<v )
			{
				root.parent_move= eachMove.move;
			}
		}
		return v;
	}
	private static Node generateNode(String move, Double score, Node root, Character[][] currentboard) {
		nodesExpanded++;
		Node child ;
		Character [][] nextPossibleMove =  new Character[size][size];
		clone2D(currentboard,nextPossibleMove,size);
		double dscore= score*score;
		int column = move.charAt(0)-65;
		int row = Integer.parseInt(move.substring(1)) -1;
		char fruit = nextPossibleMove[row][column];
		boolean visited[][] = new boolean [size][size];
		markNeighbouringFruits(nextPossibleMove, row, column, fruit,visited);
		applyGravity(nextPossibleMove, size);

		child= new Node(nextPossibleMove);
		child.setMove(move);
		child.setDepth(root.getDepth()+1);

		if(child.getDepth()%2==1)
		{
			child.setOur_score(dscore+root.getOur_score());
			child.setAdversarial_score(root.getAdversarial_score());

		}
		else if(child.getDepth()%2==0 )
		{

			child.setAdversarial_score(dscore+root.getAdversarial_score());
			child.setOur_score(root.getOur_score());



		}

		return child;
	}
	private static double Evaluation(Node root) {

		return root.our_score-root.adversarial_score;
	}

	private static boolean cutOff_test(Node root) {

		Character[][] board = root.currentboard;
		char firstChar ='*';
		for(int i=0;i<root.getSize();i++)
		{
			for(int j=0;j<root.getSize();j++)
			{
				if(board[i][j] != firstChar )
				{
					return false;
				}
			}
		}
		
		terminalReachedOrTimeOut=true;
		return true;
	}


	private static void findNextMovesWithScoresAndOrder(Character[][] markedBoard,Node root) {


		HashMap <String,Double> scoreMoveMap = new HashMap<String,Double>();
		int DisjointSetcount = 0;
		Character [][] nextPossibleMove =  new Character[size][size];
		clone2D(markedBoard,nextPossibleMove,size);
		boolean visited[][] = new boolean [size][size];
		String game_move;
		double score;
		for (int i = 0; i < size; i++){

			for (int j = 0; j < size; j++)

				if ( markedBoard[i][j] != '*' && !visited[i][j] ) {

					game_move= new String();
					score = neighbouringFruits(nextPossibleMove, i, j,visited,size,markedBoard[i][j]);
					int k=i+1;
					game_move= columnAlpha[j] +""+ k + "";
					scoreMoveMap.put(game_move, score);
					++DisjointSetcount;
				} 
		}
	
		Set<Entry<String, Double>> set = scoreMoveMap.entrySet();
		List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(set);
		Collections.sort( list, new Comparator<Map.Entry<String, Double>>()
		{
			public int compare( Map.Entry<String, Double> o1, Map.Entry<String, Double> o2 )
			{
				return (o2.getValue()).compareTo( o1.getValue() );

			}
		} );
		
		root.list= list;
	}
	private static ArrayList<Node> findNextMovesWithScores(Character[][] markedBoard,Node root,boolean justNextMove,String move) {
		if(!justNextMove)
		{
			ArrayList<Node> children = new ArrayList<Node> ();
			
			boolean visited[][] = new boolean [size][size];
			for( int f=0;f<num_fruits; f++)
			{
				char fruit = (char) (f+48);

				double score;
				for (int i = 0; i < size; i++){

					for (int j = 0; j < size; j++)

						if ( markedBoard[i][j] == fruit && !visited[i][j] ) {

							Character [][] nextPossibleMove =  new Character[size][size];
							clone2D(markedBoard,nextPossibleMove,size);
							score = neighbouringFruits(nextPossibleMove, i, j,visited,size,fruit);
							score=score*score;
							applyGravity(nextPossibleMove,size);
							Node child = new Node(nextPossibleMove);
							int k=i+1;
							child.move= columnAlpha[j] +""+ k + "";
							child.setDepth(root.getDepth()+1);
							if(child.getDepth()%2==1)
							{
								child.setOur_score(score+root.getOur_score());
								child.setAdversarial_score(root.getAdversarial_score());

							}
							else if(child.getDepth()%2==0 )
							{
								child.setAdversarial_score(score+root.getAdversarial_score());
								child.setOur_score(root.getOur_score());

							}

							children.add(child);

							
						} 

				} 

			}

			return children;
		}
		else if( move!=null)
		{

			Character [][] nextPossibleMove = new Character[size][size] ;
			clone2D(root.getCurrentboard(), nextPossibleMove , size);
			int column = move.charAt(0)-65;
			int row = Integer.parseInt(move.substring(1)) -1;
			char fruit = nextPossibleMove[row][column];
			boolean visited[][] = new boolean [size][size];
			double score = neighbouringFruits(nextPossibleMove, row, column, visited, size, fruit);
			applyGravity(nextPossibleMove, size);
			print2D(nextPossibleMove);
		}
		return null;

	}
	private static void applyGravity(Character[][] nextPossibleMove, int size) {

		char temp;
		for(int row=size-1;row>=0;row--)
		{
			for(int col=0;col<size;col++)
			{
				if(nextPossibleMove[row][col]=='*')
				{
					for(int drow=row-1; drow>=0;drow--)
					{
						if(nextPossibleMove[drow][col] !=nextPossibleMove[row][col])
						{
							temp=nextPossibleMove[drow][col];
							nextPossibleMove[drow][col] =nextPossibleMove[row][col];
							nextPossibleMove[row][col]=temp;
							break;

						}

					}
				}
			}
		}

	}
	private static void print2D(Character[][] nextPossibleMove) {
		for(int i=0;i<size; i++)
		{
			for(int j=0;j<size;j++)
			{
				System.out.print(nextPossibleMove[i][j]);
			}
			System.out.println();
		}


	}
	private static void clone2D(Character[][] markedBoard, Character[][] nextPosssibleMove, int size) {
		for(int i=0; i<size; i++)
		{
			nextPosssibleMove[i] = markedBoard[i].clone();
		}

	}
	private static double neighbouringFruits(Character[][] markedBoard, int row, int col,boolean[][] visited,int size,char fruit) {
		double cscore=0;
		if(row<0 || col<0 || row>=size ||col >=size || visited[row][col]==true || markedBoard[row][col]!=fruit ) 
		{

			return 0;
		}

		visited[row][col] = true;
		markedBoard[row][col] = '*';

		cscore++;

		return cscore+ neighbouringFruits(markedBoard, row + 1, col,visited,size,fruit)
		+neighbouringFruits(markedBoard, row - 1, col,visited,size,fruit)
		+neighbouringFruits(markedBoard, row, col + 1,visited,size,fruit)
		+neighbouringFruits(markedBoard, row, col - 1,visited,size,fruit);



	}
	private static void markNeighbouringFruits(Character[][] markedBoard, int row, int col,char fruit,boolean[][] visited) {

		if(row<0 || col<0 || row>=size ||col >=size || visited[row][col]==true || markedBoard[row][col]!=fruit ) 
		{

			return ;
		}
		visited[row][col] = true;
		markedBoard[row][col] = '*';
		markNeighbouringFruits(markedBoard, row + 1, col,fruit,visited);
		markNeighbouringFruits(markedBoard, row - 1, col,fruit,visited);
		markNeighbouringFruits(markedBoard, row, col + 1,fruit,visited);
		markNeighbouringFruits(markedBoard, row, col - 1,fruit,visited);



	}
}
