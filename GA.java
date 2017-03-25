/**
 * Do the GA to optimize the orderings
 * 
 * @author (Mengyao Liu) 
 * @ID: (15057321)
 * @version (Apr 23, 2016)
 */

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Scanner;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Arrays;
import java.io.FileNotFoundException;
import java.util.Formatter;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GA extends JFrame
{
    public static int MAX = 0; // the biggest number in edge list
    public static int [][] A; // adjacency matrix
    public static double RP = 0.85; // reproduction probability
    //public static double CP = 0.10; // crossover probability
    public static double MP = 0.05; // mutation probability
    public static int [] ordering_last_result;
    public GA()  
    {
        setTitle("AI");
        setSize(960,960);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
    public void paint(Graphics g)
    {
        int radius =100;
        int mov =200;

        double w = Math.sqrt((double)(MAX+1));
        if(Math.floor(w)<w)
        {
            w=Math.floor(w);
            w++;
        }

        for( int i=0; i < MAX+1; i++ )//index represents the node
        {
            for( int j=i+1; j < MAX+1; j++ )
            {
                if(A[i][j]==1)
                {
                    g.drawLine(
                            (int)(((double) ordering_last_result[i] % w)*radius + mov),
                            (int)(((double) ordering_last_result[i] / w)*radius + mov),
                            (int)(((double) ordering_last_result[j] % w)*radius + mov),
                            (int)(((double) ordering_last_result[j] / w)*radius + mov)
                    );
                }
            }
        }

    }
    public static void main (String args[])
            throws IOException
    {
        /**
         * read the file and find out the maximum number as the number of vertex
         */
        //find the correct file
        Path filePath = Paths.get("EdgeList.txt");
        Scanner scanner = new Scanner( filePath );

        //set MAX to zero to save the maximum number of the file

        int var;

        //find out the maximum number as the length of array
        while (scanner.hasNext())
        {
            var = scanner.nextInt();
            if( var > MAX)
            {
                MAX = var;
            }

        }
        System.out.println("This is the maximum number : "+MAX);

        /**
         * generate the Adjacency matrix which size is MAX+1 * MAX+1
         */

        //initiate edge-vertex matrix, because the vertex starts from 0, so the size is MAX+1 * MAX+1
         A = new int[ MAX+1 ][ MAX+1 ];

        //store the edge
        int row, col;
        Path filePath1 = Paths.get("EdgeList.txt");
        Scanner scanner1 = new Scanner( filePath1 );

        while (scanner1.hasNext())
        {
            row = scanner1.nextInt();
            col = scanner1.nextInt();
            A[row][col] = 1;
            A[col][row] = 1;
        }

        System.out.println("\nThis is the Adjacency matrix.\n");
        // print out the index of the array A
        System.out.print("   ");
        for(int i = 0; i<MAX+1; ++i)
        {
            System.out.printf("%2d ",i);
        }
        System.out.println();
        //print out the index and the adjacency matrix
        for(int k=0; k<MAX+1; ++k)
        {
            System.out.printf("%2d ",k);
            for(int j=0;j<MAX+1;++j)
            {
                System.out.printf("%2d ",A[k][j]);
            }
            System.out.println();
        }

        /**
         * Generate the first population
         *
         * 1. 100 unique orderings
         * 2. generate randomly
         * 3. no repeated indices
         * 4. store in G0[100][MAX+1]
         */


        Random random = new Random();
        int i,j,k;
        //G0 is used to store the first generation
        int[][] G0 = new int[100][ MAX+1 ];

        System.out.println("\nThis is the randomly generated population (100 orderings).\n");

        //the outer loop, generate 100 different orderings
        j=0;
        while( j<100 )
        {
            //use the array signal to identify whether this index is already generated
            int[] signal = new int[MAX+1];

            //generate a new order
            i = 0;
            while( i< (MAX+1) )
            {
                int r = random.nextInt(MAX+1);//0 TO 32
                if( signal[r] == 1 )
                {
                    continue;
                }
                else{
                    G0[j][i] = r;
                    signal[r] = 1;
                    ++i;
                }
            }

            //judge whether the new ordering is repeated

            boolean judge = true;
            for( k =0; k<j; ++k )
            {
                if( Arrays.equals(G0[k],G0[j]) )
                {
                    judge = false;
                    break;
                }
            }

            if( judge == false )
            {
                continue;
            }
            //when this new ordering is valid, print out the ordering
            System.out.printf("%2d   ",j);

            for( int m=0; m< (MAX+1); ++m )
            {
                System.out.printf("%2d ",G0[j][m]);
            }
            System.out.println();

            j++;

        }

        /**
         * process the generations
         *
         */
        int required_generation;
        if(args.length != 0)
            required_generation = Integer.parseInt(args[0]);// the user input the required generation
        else
            required_generation = 500;
        double[] fitnessValues;// fitness values for one generation
        int[][] G ;// the intermediate generation
        G = G0;
        int g;
        try {
                Formatter output = new Formatter("GenerationSituation.txt");
                for(  g = 0; g < required_generation; ++g )
                {
                    fitnessValues = fitnessFunction( G );// invoke method fitnessFunction()
                    output.format("%s\n", "This is the "+g+" generation.");
                    for(  i=0; i < 100; ++i )
                    {
                        output.format("%2d  ", i);
                        for( int m=0; m< (MAX+1); ++m )
                        {
                            output.format("%2d  ", G[i][m]);
                        }
                        output.format("%2f\n",fitnessValues[i]);
                    }

                    // do selection
                    G = selection( G, fitnessValues );
                    output.format("\n\n%s : %f\n\n\n\n\n\n","The lowest fitness of this generation is", fitnessValues[0]);

                    //do GA operation
                    G = operation( G );

             }
            fitnessValues = fitnessFunction( G );
            output.format("%s\n", "This is the "+g+" generation.");
            for(  i=0; i < 100; ++i )
            {
                output.format("%2d  ", i);
                for( int m=0; m< (MAX+1); ++m )
                {
                    output.format("%2d  ", G[i][m]);
                }
                output.format("%2f\n",fitnessValues[i]);
            }
            G = selection( G, fitnessValues );
            output.format("\n\n%s : %f\n\n\n\n\n\n","The lowest fitness of this generation is", fitnessValues[0]);

            ordering_last_result = new int[ MAX+1 ];
            System.arraycopy( G[0], 0, ordering_last_result, 0, MAX+1);
            output.format("%s\n", "This is ordering_last_result");
            for( i=0; i<MAX+1; ++i )
            {
                output.format("%2d ", ordering_last_result[i]);
            }
            output.close();

            int[] temp = new int[MAX+1];
            for( k = 0; k<MAX+1; ++k)
            {
                temp[k] = ordering_last_result[k];
            }
            double min=0;
            for( i=0;i<MAX+1;i++)
            {
                for( j=0;j<MAX+1;j++)
                {
                    if( temp[j]==min )
                    {
                        ordering_last_result[i]=j;
                        min++;
                        break;
                    }
                }
            }
            GA visulization = new GA();

         }
        catch (FileNotFoundException ex)   // Checked exception for Formatter
        {
            System.err.println("Unable to create file: 15057321_result.txt");
            Logger.getLogger(GA.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // calculate the fitness value for one ordering
    public static double fitnessValue( int[] ordering ){
        int k;// square table length
        int N = MAX+1;// number of nodes
        double mid = Math.sqrt( N );
        k = ( int ) Math.ceil( mid );
        double fitness = 0;// the fitness value of this ordering
        int[][] coordinates= new int[ N ][3];// store the vertex and its coordinates
        // calculate the coordinates
        for( int i =0; i < N; ++i )
        {
            coordinates[i][0] = ordering[i];// the vertex

            coordinates[i][1] = i%k;// the x_coordinate

            coordinates[i][2] = i/k;// the y_coordinate
            //System.out.printf("Index is %2d: Node: %2d: x:%d y:%d \n",i,coordinates[i][0], coordinates[i][1], coordinates[i][2] );
        }
        //
        for( int i = 0; i < N; i++ )
        {
            int m = coordinates[i][0];

            for( int j = 0; j < N; j++ )
            {
                int n = coordinates[j][0];
                if( A[m][n] == 1 )// there is a link between vertex m and n
                {
                    double L =  Math.sqrt( (coordinates[i][1]-coordinates[j][1])*(coordinates[i][1]-coordinates[j][1])+
                            (coordinates[i][2]-coordinates[j][2])*(coordinates[i][2]-coordinates[j][2]) );
                    fitness = fitness + L;
                }
            }
        }

        return fitness;// return the fitness value
    }
    // calculate all orderings fitness values
    public static double[] fitnessFunction( int[][] G )
    {
        double[] fitness = new double[ 100 ];
        for( int i = 0; i < 100; i++ )
        {
            fitness[i] = fitnessValue( G[i] );
        }
        return fitness;
    }

    // select the current generation
    public static int[][] selection( int[][] G, double[] fitness )
    {
        int N = MAX+1;
        double d;
        int[] copied = new int[ N ];

        // using insert sort to sort the generation and the fitness value
        for ( int i = 0; i < 100; i++ )
        {
            for ( int j = i; j > 0 && (fitness[j]<fitness[j-1]); j-- )
            {
                //exchange the fitness value
                d = fitness[j];
                fitness[j] = fitness[j-1];
                fitness[j-1] = d;
                //exchange the ordering in G[][]
                System.arraycopy( G[j], 0, copied, 0, N);
                System.arraycopy( G[j-1], 0, G[j], 0, N);
                System.arraycopy( copied, 0, G[j-1], 0, N);
            }
        }
        // exchange the last third by first third ( 33 orderings )
        for( int i = 0, j = 67; i < 33 && j < 100 ; i++, j++ )
        {
            System.arraycopy( G[i], 0, G[j], 0, 33);
            fitness[j] = fitness[i];
        }
        return G;
    }
    // do GA operations( mutation; crossover; reproduction )
    public static int[][] operation( int[][] G )
    {
        int[][] result = new int[ 100 ][ MAX + 1 ]; // to store the result, namely next generation
        Random random = new Random();
        double r;// generate the random number to choose the ordering
        int[] used = new int[100];// 1 for used, 0 means it is not used
        int r1;// the index for orderings
        int r2;// the index for orderings
        int g = 0;

        while(  g < 100 ) // 100 orderings in the mating pool
        {
            r = Math.random();
            if ( r < RP ) // do the reproduction
            {
                r1 = random.nextInt(100); // randomly choose one ordering
                while( used[r1] == 1)
                {
                    r1 = random.nextInt(100);
                }
                used[r1] = 1;// when the ordering in the selected population was used, set the value for used[] to one.
                System.arraycopy( G[r1], 0, result[g], 0, MAX+1);// pick one ordering from selected population and add it to next generation
            }
            else if (r < RP+MP && r >= RP) // do the mutation
            {
                r1 = random.nextInt(100); // randomly choose one ordering
                while( used[r1] == 1)
                {
                    r1 = random.nextInt(100);
                }
                used[r1] = 1;// when the ordering in the selected population was used, set the value for used[] to one.
                int index1 = random.nextInt(MAX+1);
                int index2 = random.nextInt(MAX+1);
                while( index2 == index1)
                {
                    index2 = random.nextInt(MAX+1);
                }
                int con;
                con = G[r1][index1];
                G[r1][index1] = G[r1][index2];
                G[r1][index2] = con;
                System.arraycopy( G[r1], 0, result[g], 0, MAX+1);
            }
            else // do the crossover ( alternating position )
            {
                if ( g == 99 )
                {
                    continue;
                }
                r1 = random.nextInt(100); // randomly choose one ordering
                while( used[r1] == 1)
                {
                    r1 = random.nextInt(100);
                }
                used[r1] = 1;// when the ordering in the selected population was used, set the value for used[] to one.
               // System.out.print(g+" " +r1+ "\n");
                r2 = random.nextInt(100); // randomly choose another ordering
                while( used[r2] == 1)
                {
                    r2 = random.nextInt(100);
                }
                used[r2] = 1;

                int i=0, j=0, k=0;// first parent use i, second parent use j, child use k.
                int [] signal1 = new int[MAX+1];
                int [] signal2 = new int[MAX+1];

                boolean first = true;
                int var;
                while( k<MAX+1 )//generate the first child
                {
                    if( first ) // it's first parent turn
                    {
                        var = G[r1][i];
                        if( signal1[var] == 1 )//if this value already exists
                        {
                            ++i;
                            first = false;
                        }
                        else{
                            result[g][k] = var;
                            signal1[var]=1;
                            ++k;
                            ++i;
                            first = false;
                        }
                    }
                    else // it's second parent turn
                    {
                        var = G[r2][j];
                        if( signal1[var] == 1 )//if this value already exists
                        {
                            ++j;
                            first = true;
                        }
                        else{
                            result[g][k] = var;
                            signal1[var] =1;
                            ++k;
                            ++j;
                            first = true;
                        }
                    }
                }
                i=0;
                j=0;
                k=0;
                first = false;
                ++g; // next child
                while( k<MAX+1 )//generate the second child
                {
                    if( first ) // it's first parent turn
                    {
                        var = G[r1][i];
                        if( signal2[var] == 1 )//if this value already exists
                        {
                            ++i;
                            first = false;
                        }
                        else{
                            result[g][k] = var;
                            signal2[var] = 1;
                            ++k;
                            ++i;
                            first = false;
                        }
                    }
                    else // it's second parent turn
                    {
                        var = G[r2][j];
                        if( signal2[var] == 1 )//if this value already exists
                        {
                            ++j;
                            first = true;
                        }
                        else{
                            result[g][k] = var;
                            signal2[var] = 1;
                            ++k;
                            ++j;
                            first = true;
                        }
                    }
                }
            }
            ++g;
        }
        return result;
    }// end of operation method
}
