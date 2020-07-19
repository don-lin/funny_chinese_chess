import javax.swing.JFrame;
import javax.swing.JComponent;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.util.LinkedList;
import java.util.Stack;
import java.awt.event.MouseEvent;

class Step{
	public Step(Move nowMove,byte chess) {
		move=nowMove;
		chessType=chess;
	}
	Move move;
	byte chessType;
}

class Move{
	int fromx,fromy;
	int tox,toy;
	public Move(int fromX,int fromY,int toX,int toY) {
		fromx=fromX;
		fromy=fromY;
		tox=toX;
		toy=toY;
	}
	public boolean equals(Move p) {
		return fromx==p.fromx&&fromy==p.fromy&&tox==p.tox&&toy==p.toy;
	}
	public String toString() {
		return "("+fromx+","+fromy+")->"+"("+tox+","+toy+") ";
	}
}

class calculate extends Thread{
	chessBoard board;
	chessBoard boardInput;
	ChinaChess pic;
	byte a=-1;
	@SuppressWarnings("unchecked")
	private void copyBoard(chessBoard target,chessBoard resource) {
		for(int x=0;x<9;x++)
			for(int y=0;y<10;y++)
				target.board[x][y]=resource.board[x][y];

		target.enemy=resource.enemy;
		target.ally=resource.ally;
		target.history=(Stack<Step>)(resource.history.clone());
		target.time=resource.time;
		target.maximumScore=resource.maximumScore;		
	}
	public void changeAlly() {
		a=(byte)-a;
	}
	public calculate(chessBoard boardNeedCalculate,ChinaChess Pic) {
		boardInput=boardNeedCalculate;
		board=new chessBoard();
		copyBoard(board,boardInput);
		pic=Pic;
		pic.calculatePercentage=0;
	}
	public void run() {
	board.setAlly(a);
	pic.calculatePercentage=0;
	pic.onCalculating=true;
    
    Stack<Move> l;
	    l=board.getStepList(board.ally);
	 Move maxMove=null;
	 int maxValue=-chessBoard.INF;
	 long time1=System.nanoTime();
	 
	 multiCalculate[] myCalculate=new multiCalculate[l.size()];
	 
	 Move m=null;
	 for(int i=0;i<myCalculate.length;i++) {
		 m=l.pop();
		 board.move(m);
		 myCalculate[i]=new multiCalculate(board,m,5);
		 myCalculate[i].start();
		 board.unmove();
	 }
	 for(int i=0;i<myCalculate.length;i++) {
		 try {
			myCalculate[i].join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		 pic.calculatePercentage=(float)i/myCalculate.length;
		 pic.repaint();
	 }
	 for(int i=0;i<myCalculate.length;i++) {
		 if(myCalculate[i].score>maxValue) {
			 maxValue=myCalculate[i].score;
			 maxMove=myCalculate[i].move;
		 }
	 }
	 
	 board.move(maxMove);
	 board.maximumScore=maxValue;
	 board.time=((System.nanoTime()-time1)/10000000);
	 board.time/=100;
	//	board.doMinMaxMulti(chessBoard.BLACK, 99);
		copyBoard(boardInput,board);
		pic.onCalculating=false;
		pic.repaint();
	}
}
class multiCalculate extends Thread{
	chessBoard board;
	public int score;
	public int depth;
	public Move move;
	private void copyBoard(chessBoard target,chessBoard resource) {
		for(int x=0;x<9;x++)
			for(int y=0;y<10;y++)
				target.board[x][y]=resource.board[x][y];
		target.enemy=resource.enemy;
		target.ally=resource.ally;	
	}
	public multiCalculate(chessBoard boardInput,Move m,int Depth) {
		board=new chessBoard();
		copyBoard(board,boardInput);
		depth=Depth;
		move=m;
	}
	public void run() {
		score=board.alphaBeta(depth, -chessBoard.INF, chessBoard.INF);
		//score=-board.NegaAlphaBeta(depth,  -chessBoard.INF, chessBoard.INF);
	}
}

class chessBoard {
	public final static byte King = 100;//帅
	public final static byte Rook = 20;//车
	public final static byte Cannon = 10;//炮
	public final static byte Knight = 9;//马
	public final static byte Bishop = 3;//象
	public final static byte Mandarin = 2;//士
	public final static byte Pawn = 1;//兵
	public final static byte Empty = 0;//空
	public final static byte BLACK = -1;
	public final static byte RED = 1;
	
	public static final int INF=9999;
	public byte enemy;
	public byte ally;

	public byte[][] board;
	public Stack<Step> history;
	
	public float time;
	public int maximumScore;

	public chessBoard() {
		board=new byte[9][10];
		history=new Stack<Step>();
		for(int x=0;x<board.length;x++)
			for(int y=0;y<board[x].length;y++)
			board[x][y] = Empty;//空
	}

	public void setAlly(byte allyType) {
		ally=allyType;
		enemy=(byte)-allyType;
	}
	
	int NegaAlphaBeta(int depth, int alpha, int beta)
	{

		int a,b,value;
		
	    if(depth <= 0 )
		{
			return evaluation();
		}
		
	    	//int val=-NegaAlphaBeta(depth-1-2,-beta,-beta+1);
	    	//if(val>=beta) {
	    	//	return val;
	    	//}
	    	

		    Stack<Move> l;	    
		    if( depth % 2  == 0) {
			    l=getStepList(ally);
		    }
		    else
		    	l=getStepList(enemy);
		
	    a = alpha;
	    b = beta;
	    for (int i = 0; !l.isEmpty(); i++ ) 
		{
	    	Move m=l.pop();
			move(m);
			value = -NegaAlphaBeta(depth-1 , -b, -a );
			
			if (value > a && value < beta && i > 0) 
			{
				a = -NegaAlphaBeta (depth-1, -beta, -value );
			}
			unmove();
			if (a < value)
			{
				a=value;
			}
			if ( a >= beta ) 
			{
				return a;
			}       
			b = a + 1;
		}
		return a;
	}
	
	public int alphaBeta(int depth,int alpha,int beta)
	{
	    int value;
	    
	    if(depth <= 0 )
	        return evaluation();
	    
	    Stack<Move> l;
	    
	    if( depth % 2  == 0) {
		    l=getStepList(ally);
	        while(!l.isEmpty())
		    {
		    	Move m=l.pop();
		        move(m);
		        value =alphaBeta(depth-1,alpha,beta);
		        unmove();
		        if(value>alpha)
		        	alpha=value;
		        if(alpha>=beta)
		        	break;
		    }
		        return alpha;
	    }
	    
	    else {
		    l=getStepList(enemy);
		    while(!l.isEmpty())
		    {
		    	Move m=l.pop();
		    	move(m);
		        value =alphaBeta(depth-1,alpha,beta);
		        unmove() ;
		        if(value<beta)
		        	beta=value;
		        if(alpha>=beta)
		        	break;
		    }
		        return beta;
	    }
	}
	
	public void doMinMaxMulti(byte chessType,int depth) {
		setAlly(chessType);
	    
	    Stack<Move> l;
		    l=getStepList(chessType);
		 Move maxMove=null;
		 int maxValue=-INF;
		 long time1=System.nanoTime();
		 
		 multiCalculate[] myCalculate=new multiCalculate[l.size()];
		 
		 Move m=null;
		 for(int i=0;i<myCalculate.length;i++) {
			 m=l.pop();
			 move(m);
			 myCalculate[i]=new multiCalculate(this,m,depth);
			 myCalculate[i].start();
			 unmove();
		 }
		 for(int i=0;i<myCalculate.length;i++) {
			 try {
				myCalculate[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			 System.out.println((float)i/myCalculate.length);
		 }
		 for(int i=0;i<myCalculate.length;i++) {
			 if(myCalculate[i].score>maxValue) {
				 maxValue=myCalculate[i].score;
				 maxMove=myCalculate[i].move;
			 }
		 }
		 
	     move(maxMove);
	     maximumScore=maxValue;
	     time=((System.nanoTime()-time1)/10000000);
	     time/=100;
	}
	
	public void doMinMax(byte chessType,int depth) {
		setAlly(chessType);
	    
	    Stack<Move> l;
		    l=getStepList(chessType);
		 int value;
		 Move maxMove=null;
		 int maxValue=-INF;
		 long time1=System.nanoTime();
	     while(!l.isEmpty())
	    {
	    	Move m=l.pop();
	        move(m);
	        value = alphaBeta(depth,-INF,INF);
	        if(value>maxValue) {
	        	maxMove=m;
	        	maxValue=value;
	        }
	        unmove();
	    }
	     move(maxMove);
	     maximumScore=maxValue;
	     time=((System.nanoTime()-time1)/10000000);
	     time/=100;
	}
	
	public int MinMax(chessBoard p , int depth)
	{
	    int bestvalue , value;
	    
	    if( depth <= 0 )
	        return p.evaluation();
	    
	    Stack<Move> l;
	    if( depth % 2  == 0) {
		    l=p.getStepList(ally);
	        bestvalue = - INF;
	    }
	    else {
	        bestvalue = INF ;
		    l=p.getStepList(enemy);
	    }
	     while(!l.isEmpty())
	    {
	    	Move m=l.pop();
	        p.move(m);
	        value = MinMax(p,depth-1);
	        p.unmove() ;
	        if(depth % 2 == 0) {
	        	if(value>bestvalue)
	        		bestvalue=value;
	        }
	        else {
	        	if(value<bestvalue)
	        		bestvalue=value;
	        }
	    }
	    return bestvalue;
	}
	
	public int evaluation() {
		int score=0;
		for(int x=0;x<9;x++)
			for(int y=0;y<10;y++)
				score+=board[x][y];
		if(ally==-1)
			score=-score;
		return score;
	}
	
	public void init() {
		board[0][0]=Rook;
		board[1][0]=Knight;
		board[2][0]=Bishop;
		board[3][0]=Mandarin;
		board[4][0]=King;
		board[1][2]=Cannon;
		board[0][3]=board[2][3]=board[4][3]=Pawn;
		for(int y=0;y<5;y++)
		for(int x=0;x<5;x++)
			board[x][9-y]=(byte) -board[x][y];
		
		for(int y=0;y<10;y++)
		for(int x=0;x<4;x++)
			board[8-x][y]=board[x][y];
		
	}
	
	
	public void move(Move m) {
		byte chess=board[m.tox][m.toy];
		Step nowStep=new Step(m,chess);
		history.push(nowStep);

		board[m.tox][m.toy]=board[m.fromx][m.fromy];
		board[m.fromx][m.fromy]=Empty;
	}
	
	public void unmove() {
		if(history.isEmpty())return;
		
		Step nowStep=history.pop();
		
		board[nowStep.move.fromx][nowStep.move.fromy]=board[nowStep.move.tox][nowStep.move.toy];
		board[nowStep.move.tox][nowStep.move.toy]=nowStep.chessType;
	}
	
	public Stack<Move> getStepList(byte chessType){
		Stack<Move> e=new Stack<Move>();
		for(int x=0;x<9;x++)
			for(int y=0;y<10;y++) {
				if((board[x][y]*chessType)>0) {
					byte chess=board[x][y];
					if(chess<0)
						chess=(byte) -chess;
					if(chess==Rook) {
						for(int i=1;x+i<9;i++) {
							if(board[x+i][y]*chessType>0)
								break;
							e.push(new Move(x,y,x+i,y));
							if(board[x+i][y]!=Empty)
								break;
						}
						for(int i=1;x-i>=0;i++) {
							if(board[x-i][y]*chessType>0)
								break;
							e.push(new Move(x,y,x-i,y));
							if(board[x-i][y]!=Empty)
								break;
						}
						for(int i=1;y+i<10;i++) {
							if(board[x][y+i]*chessType>0)
								break;
							e.push(new Move(x,y,x,y+i));
							if(board[x][y+i]!=Empty)
								break;
						}
						for(int i=1;y-i>=0;i++) {
							if(board[x][y-i]*chessType>0)
								break;
							e.push(new Move(x,y,x,y-i));
							if(board[x][y-i]!=Empty)
								break;
						}
					}
					else if(chess==Knight) {
						if(!get(x+1,y)) {
							if(canGo(x+2,y+1,chessType))e.push(new Move(x,y,x+2,y+1));
							if(canGo(x+2,y-1,chessType))e.push(new Move(x,y,x+2,y-1));
						}
						if(!get(x-1,y)) {
							if(canGo(x-2,y+1,chessType))e.push(new Move(x,y,x-2,y+1));
							if(canGo(x-2,y-1,chessType))e.push(new Move(x,y,x-2,y-1));
						}
						if(!get(x,y+1)) {
							if(canGo(x+1,y+2,chessType))e.push(new Move(x,y,x+1,y+2));
							if(canGo(x-1,y+2,chessType))e.push(new Move(x,y,x-1,y+2));
						}
						if(!get(x,y-1)) {
							if(canGo(x+1,y-2,chessType))e.push(new Move(x,y,x+1,y-2));
							if(canGo(x-1,y-2,chessType))e.push(new Move(x,y,x-1,y-2));
						}
					}
					else if(chess==Bishop) {
						if(!get(x+1,y+1)&&y!=4) {
							if(canGo(x+2,y+2,chessType))e.push(new Move(x,y,x+2,y+2));
						}
						if(!get(x-1,y+1)&&y!=4) {
							if(canGo(x-2,y+2,chessType))e.push(new Move(x,y,x-2,y+2));
						}
						if(!get(x+1,y-1)&&y!=5) {
							if(canGo(x+2,y-2,chessType))e.push(new Move(x,y,x+2,y-2));
						}
						if(!get(x-1,y-1)&&y!=5) {
							if(canGo(x-2,y-2,chessType))e.push(new Move(x,y,x-2,y-2));
						}
					}
					else if(chess==Cannon) {
						for(int i=1;x+i<9;i++) {
							if(board[x+i][y]!=Empty){
								i++;
								while(x+i<9){
									if(board[x+i][y]!=Empty){
										if(board[x+i][y]*chessType<0)
											e.push(new Move(x,y,x+i,y));
										break;
									}
									i++;
								}
								break;
							}
							e.push(new Move(x,y,x+i,y));
						}
						for(int i=1;x-i>=0;i++) {
							if(board[x-i][y]!=Empty){
								i++;
								while(x-i>=0){
									if(board[x-i][y]!=Empty){
										if(board[x-i][y]*chessType<0)
											e.push(new Move(x,y,x-i,y));
										break;
									}
									i++;
								}
								break;
							}
							e.push(new Move(x,y,x-i,y));
						}
						for(int i=1;y+i<10;i++) {
							if(board[x][y+i]!=Empty){
								i++;
								while(y+i<10){
									if(board[x][y+i]!=Empty){
										if(board[x][y+i]*chessType<0)
											e.push(new Move(x,y,x,y+i));
										break;
									}
									i++;
								}
								break;
							}
							e.push(new Move(x,y,x,y+i));
						}
						for(int i=1;y-i>=0;i++) {
							if(board[x][y-i]!=Empty){
								i++;
								while(y-i>=0){
									if(board[x][y-i]!=Empty){
										if(board[x][y-i]*chessType<0)
											e.push(new Move(x,y,x,y-i));
										break;
									}
									i++;
								}
								break;
							}
							e.push(new Move(x,y,x,y-i));
						}						
					}
					else if(chess==Mandarin) {
						if(canGo(x+1,y+1,chessType)&&inPalace(x+1,y+1))e.push(new Move(x,y,x+1,y+1));
						if(canGo(x-1,y+1,chessType)&&inPalace(x-1,y+1))e.push(new Move(x,y,x-1,y+1));
						if(canGo(x+1,y-1,chessType)&&inPalace(x+1,y-1))e.push(new Move(x,y,x+1,y-1));
						if(canGo(x-1,y-1,chessType)&&inPalace(x-1,y-1))e.push(new Move(x,y,x-1,y-1));
					}
					else if(chess==King) {
						if(canGo(x+1,y,chessType)&&inPalace(x+1,y))e.push(new Move(x,y,x+1,y));
						if(canGo(x-1,y,chessType)&&inPalace(x-1,y))e.push(new Move(x,y,x-1,y));
						if(canGo(x,y+1,chessType)&&inPalace(x,y+1))e.push(new Move(x,y,x,y+1));
						if(canGo(x,y-1,chessType)&&inPalace(x,y-1))e.push(new Move(x,y,x,y-1));
						byte myKing=board[x][y];
						for(int i=1;y+i<10;i++) {
							if(board[x][y+i]!=Empty) {
								if(board[x][y+i]==-myKing)
									e.push(new Move(x,y,x,y+i));
								break;
							}
						}
						for(int i=1;y-i>=0;i++) {
							if(board[x][y-i]!=Empty) {
								if(board[x][y-i]==-myKing)
									e.push(new Move(x,y,x,y-i));
								break;
							}
						}
					}
					else if(chess==Pawn) {
						if(chessType==RED){
								if(canGo(x,y+1,chessType))e.push(new Move(x,y,x,y+1));
							if(y>4){
								if(canGo(x-1,y,chessType))e.push(new Move(x,y,x-1,y));
								if(canGo(x+1,y,chessType))e.push(new Move(x,y,x+1,y));								
							}
						}
						if(chessType==BLACK){
								if(canGo(x,y-1,chessType))e.push(new Move(x,y,x,y-1));
							if(y<5){
								if(canGo(x-1,y,chessType))e.push(new Move(x,y,x-1,y));
								if(canGo(x+1,y,chessType))e.push(new Move(x,y,x+1,y));								
							}
						}
					}
				}
			}
		Stack<Move> result=new Stack<Move>();
		Stack<Move> resultEat=new Stack<Move>();
		while(!e.isEmpty()) {
			Move s=e.pop();
			if(board[s.tox][s.toy]!=Empty)
				resultEat.push(s);
			else
				result.push(s);
		}
		while(!resultEat.isEmpty())
			result.push(resultEat.pop());
		return result;
	}
	public boolean inPalace(int x,int y){
			if(x>=3&&x<=5&&((y>=0&&y<=2)||(y>=7&&y<=9)))
				return true;
			return false;
	}
	public boolean canGo(int x,int y,byte chessType) {
		if(x<0||x>8)
			return false;
		if(y<0||y>9)
			return false;
		if(board[x][y]*chessType<=0)
			return true;
		return false;
		
	}
	public boolean get(int x,int y) {
		if(x<0||x>8)
			return true;
		if(y<0||y>9)
			return true;
		return board[x][y]!=Empty;		
	}
	public void overTurn() {
		return;
	}
}

public class ChinaChess extends JComponent implements MouseListener {
	private static final long serialVersionUID = 1L;
	static int a = 60;
	static int b = 20;
	public chessBoard board;
	public boolean onCalculating;
	public float calculatePercentage;
	
	private int tempx,tempy;
	private Rectangle undo;
	private Rectangle redMove;
	private Rectangle blackMove;
	private Rectangle autoRun;
	private Rectangle restart;
	
	private boolean autoRunFlag;
	public ChinaChess(){
		board=new chessBoard();
		board.init();
		tempx=-1;
		undo=new Rectangle(a,11*a,a,a/2);
		redMove=new Rectangle(2*a,11*a,a,a/2);
		blackMove=new Rectangle(3*a,11*a,a,a/2);
		autoRun=new Rectangle(4*a,11*a,a,a/2);
		restart=new Rectangle(5*a,11*a,a,a/2);
		autoRunFlag=true;
		onCalculating=false;
	}

	public void mouseClicked(MouseEvent e) {
	}

	private boolean check(Move m) {
		byte chessType=chessBoard.RED;
		if(m.fromx<0||m.fromy<0||m.fromx>8||m.fromy>9)
			return false;
		if(board.board[m.fromx][m.fromy]<0)
			chessType=chessBoard.BLACK;
		Stack<Move> a=board.getStepList(chessType);
		for(Move b:a)if(b.equals(m))return true;
		return false;
	}
	
	public void mousePressed(MouseEvent e) {
	//	e.translatePoint(0, -25);
		e.translatePoint(-10, -30);
		Point p = e.getPoint();
		if(undo.contains(p))
			board.unmove();
		if(autoRun.contains(p))
			autoRunFlag=!autoRunFlag;
		if(redMove.contains(p)) {
			calculate c=new calculate(board,this);
			c.changeAlly();
			c.start();
		}
		if(blackMove.contains(p)) {
			calculate c=new calculate(board,this);
			c.start();
		}
		if(restart.contains(p)) {
			board=new chessBoard();
			board.init();
		}
			
		if(tempx==-1){
			tempx=p.x/a-1;
			tempy=p.y/a-1;
			if(p.x%a>a-b)
				tempx++;
			if(p.y%a>a-b)
				tempy++;
			tempy=9-tempy;
			if((p.x%a<a-b&&p.x%a>b)||(p.y%a<a-b&&p.y%a>b))
				tempx=-1;
		}
		else {
			int nowx=p.x/a-1;
			int nowy=p.y/a-1;
			if(p.x%a>a-b)
				nowx++;
			if(p.y%a>a-b)
				nowy++;
			nowy=9-nowy;
			if((p.x%a<a-b&&p.x%a>b)||(p.y%a<a-b&&p.y%a>b))
				return;
			Move m=new Move(tempx,tempy,nowx,nowy);
			if(check(m)) {
			board.move(m);
			if(autoRunFlag) {
			onCalculating=true;
			calculate c=new calculate(board,this);
			c.start();
			}
			}
			//System.out.println("Move:"+m);
			//System.out.println("score:"+board.evaluation());
			tempx=-1;
		}
		repaint();	
	}

	public void mouseReleased(MouseEvent e) {}

	public void mouseEntered(MouseEvent e) {}

	public void mouseExited(MouseEvent e) {}
	
	void fillRect(Rectangle r,Color c,Graphics g) {
		g.setColor(c);
		g.fillRect(r.x,r.y,r.width,r.height);
	}
	
	void fillRect(Rectangle r,Color c,Graphics g,String name) {
		g.setColor(c);
		g.fillRect(r.x, r.y, r.width, r.height);
		
		g.setFont(new Font("Verdana",10,10));
		g.setColor(Color.WHITE);
		g.drawString(name, r.x+a/8, r.y+a/3);
	}
	
	void drawChess(Graphics g) {
		for(int i=1;i<10;i++) {
			g.drawLine(i*a, a, i*a, 5*a);
			g.drawLine(i*a, 6*a, i*a, 10*a);
		}
		for(int i=1;i<11;i++) {
			g.drawLine(a, i*a, 9*a, i*a);
		}
		g.drawLine(a, 5*a, a, 6*a);
		g.drawLine(9*a, 5*a, 9*a, 6*a);
		g.drawLine(4*a, a, 6*a, 3*a);
		g.drawLine(4*a, 3*a, 6*a, a);
		g.drawLine(4*a, 8*a, 6*a, 10*a);
		g.drawLine(4*a, 10*a, 6*a, 8*a);

		g.setFont(g.getFont().deriveFont((float) b));
		for(int x=0;x<9;x++)
			for(int y=0;y<10;y++) {
				if(board.board[x][y]!=chessBoard.Empty) {
				int chess=board.board[x][y];
				if(chess<0) {
					g.setColor(Color.BLACK);
					chess=-chess;
				}
				else
					g.setColor(Color.RED);
				g.fillOval((x+1)*a-b, (10-y)*a-b, b*2, b*2);
				g.setColor(Color.WHITE);
				String temp="";
				switch(chess) {
				case chessBoard.Pawn:temp="兵";break;
				case chessBoard.Rook:temp="车";break;
				case chessBoard.Cannon:temp="炮";break;
				case chessBoard.Knight:temp="马";break;
				case chessBoard.Bishop:temp="象";break;
				case chessBoard.Mandarin:temp="士";break;
				case chessBoard.King:temp="帅";break;
				default: break;
				}
				g.drawString(temp, (x+1)*a-b/2,(10-y)*a+b/2);
				}
			}
		g.setColor(Color.WHITE);
		for(int x=0;x<9;x++) {
			g.drawString(""+x, (x+1)*a,10*a+2*b);			
		}
		for(int y=0;y<10;y++) {
			g.drawString(""+y, a-2*b,(10-y)*a);			
		}
		fillRect(undo,new Color(171,212,22),g,"undo");
		fillRect(redMove,Color.PINK,g,"P-move");
		fillRect(blackMove,Color.GRAY,g,"B-move");
		if(autoRunFlag)
			fillRect(autoRun,new Color(101,122,251),g,"AutoRun");
		else
			fillRect(autoRun,Color.BLACK,g,"AutoRun");
		fillRect(restart,new Color(23,227,193),g,"Restart");
		
		g.setColor(Color.BLACK);
		g.drawString(board.time+" s", 6*a, 11*a+a/4);
		g.drawString(board.history.size()+" steps", 6*a, 11*a+a/2);
		g.drawString("now your score is:"+Integer.toString((-board.evaluation())), 7*a, 11*a+a/4);
		g.drawString("max score after fighting:"+(-board.maximumScore), 7*a, 11*a+a/2);

		if(onCalculating) {
			g.setColor(Color.BLACK);
			g.drawRect(4*a,6*a-a/3,3*a,a/4);
			g.fillRect(4*a,6*a-a/3,(int) (3*a*calculatePercentage),a/4);
			g.setColor(Color.RED);
			g.drawString("on calculating "+(float)((int)(calculatePercentage*100))+"%", 4*a, 5*a+a/2);
		}
		
		
		if(!board.history.isEmpty()) {
				g.setColor(Color.YELLOW);
				Move lastMove=board.history.peek().move;
				g.drawRect((lastMove.tox+1)*a-b, (10-lastMove.toy)*a-b, 2*b, 2*b);
				g.drawRect((lastMove.fromx+1)*a-b, (10-lastMove.fromy)*a-b, 2*b, 2*b);		
		}
		
		if(tempx!=-1&&!(tempx<0||tempx>8||tempy<0||tempy>9)) {
			Stack<Move> e;				
			if(board.board[tempx][tempy]<0)
				e = board.getStepList(chessBoard.BLACK);
			else
				e = board.getStepList(chessBoard.RED);
		g.setColor(Color.GREEN);
		while(!e.isEmpty()) {
			if(e.peek().fromx==tempx&&e.peek().fromy==tempy) {
				g.drawOval((e.peek().tox+1)*a-b/2, (10-e.peek().toy)*a-b/2, b, b);
			}
		e.pop();
		}
		}
	}
	
	public void paint(Graphics mygraph) {
		drawChess(mygraph);
	}

	public static void main(String[] args) {
		JFrame window = new JFrame();
		window.setSize(10*a, 13*a);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ChinaChess pic = new ChinaChess();
		window.add(pic);
		window.addMouseListener(pic);
		window.setVisible(true);
	}
}