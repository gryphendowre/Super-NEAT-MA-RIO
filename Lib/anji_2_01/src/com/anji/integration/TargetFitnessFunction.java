/*
 * Copyright (C) 2004 Derek James and Philip Tucker
 * 
 * This file is part of ANJI (Another NEAT Java Implementation).
 * 
 * ANJI is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 * 
 * created by Philip Tucker
 */
package com.anji.integration;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import com.anji.util.Configurable;
import com.anji.util.Properties;
import com.anji.util.Randomizer;
import com.superneatmario.SimANJI;

/**
 * Determines fitness based on how close <code>Activator</code> output is to a target.
 * 
 * @author Philip Tucker
 */
public abstract class TargetFitnessFunction implements BulkFitnessFunction, Configurable {

private static Logger logger = Logger.getLogger( TargetFitnessFunction.class );

private final static String ADJUST_FOR_NETWORK_SIZE_FACTOR_KEY = "fitness.function.adjust.for.network.size.factor";

private float adjustForNetworkSizeFactor = 0.0f;

/**
 * properties key, file containing strimuli
 */
public final static String STIMULI_FILE_NAME_KEY = "stimuli.file";

/**
 * properties key, file containing output targets
 */
public final static String TARGETS_FILE_NAME_KEY = "targets.file";

private final static String TARGETS_RANGE_KEY = "targets.range";

/**
 * dimension # training sets by dim stimuli
 */
private double[][] stimuli;

/**
 * dimension # training sets by dim response
 */
private double[][] targets;

private double targetRange = 0.0d;

private int maxFitnessValue;

private ActivatorTranscriber activatorFactory;

private Randomizer randomizer;

private final static double COIN_ALPHA = 2.0; 
/**
 * See <a href=" {@docRoot}/params.htm" target="anji_params">Parameter Details </a> for
 * specific property settings.
 * 
 * @param props configuration parameters
 */
public void init( Properties props ) {
	try {
		randomizer = (Randomizer) props.singletonObjectProperty( Randomizer.class );
		activatorFactory = (ActivatorTranscriber) props
				.singletonObjectProperty( ActivatorTranscriber.class );

		//stimuli = Properties.loadArrayFromFile( props.getResourceProperty( STIMULI_FILE_NAME_KEY ) );
		//targets = Properties.loadArrayFromFile( props.getResourceProperty( TARGETS_FILE_NAME_KEY ) );
		targets = new double [6][6]; 
		//targetRange = props.getDoubleProperty( TARGETS_RANGE_KEY, 0.0d );
		adjustForNetworkSizeFactor = props.getFloatProperty( ADJUST_FOR_NETWORK_SIZE_FACTOR_KEY,
				0.0f );

		/*if ( stimuli.length == 0 || targets.length == 0 )
			throw new IllegalArgumentException( "require at least 1 training set for stimuli ["
					+ stimuli.length + "] and targets [" + targets.length + "]" );
		if ( stimuli.length != targets.length )
			throw new IllegalArgumentException( "# training sets does not match for stimuli ["
					+ stimuli.length + "] and targets [" + targets.length + "]" );*/
	}
	catch ( Exception e ) {
		throw new IllegalArgumentException( "invalid properties: " + e.getClass().toString() + ": "
				+ e.getMessage() );
	}
}

/**
 * @param aMaxFitnessValue maximum raw fitness this function will return
 */
protected void setMaxFitnessValue( int aMaxFitnessValue ) {
	int minGenes = 2; //stimuli[ 0 ].length + targets[ 0 ].length;
	maxFitnessValue = aMaxFitnessValue - (int) ( adjustForNetworkSizeFactor * minGenes );
}

/**
 * Iterates through chromosomes. For each, transcribe it to an <code>Activator</code> and
 * present the stimuli to the activator. The stimuli are presented in random order to ensure the
 * underlying network is not memorizing the sequence of inputs. Calculation of the fitness based
 * on error is delegated to the subclass. This method adjusts fitness for network size, based on
 * configuration.
 * 
 * @param genotypes <code>List</code> contains <code>Chromosome</code> objects.
 * @see TargetFitnessFunction#calculateErrorFitness(double[][], double, double)
 */


final public void evaluate( List genotypes, int generation ) {
	Iterator it = genotypes.iterator();
	int seed = new Random().nextInt(); 
	int genomeNum = 0; 
	//ExecutorService service = Executors.newFixedThreadPool(1);
	double maxFitness = -1.0; 
	while ( it.hasNext() ) {	
		Chromosome genotype = (Chromosome) it.next();
//		System.out.println("Specie  " + genotype.getSpecie().getRepresentativeId()); 
	    //new EvalThreadTask(activatorFactory, genotype, genomeNum, seed, generation);	
		try {
			Activator activator = activatorFactory.newActivator( genotype ); 
			SimANJI sa = new SimANJI(activator, seed, genomeNum, generation, 0); 
			boolean isDone = sa.start();
			//sa.start();
			double [][] responses = null; 
			
			if(isDone)
			{
				/*responses = new double [sa.getResponses().length][sa.getResponses().length]; 
				activator = sa.getActivator(); 
				for (int i = 0; i <  sa.getResponses().length;i++)
					responses[i] = sa.getResponses(); */
			
			
			//after death, or win, fitness = distance mario made
				//calculateErrorFitness( responses, activator.getMinResponse(),
				//activator.getMaxResponse()
				//System.out.println("Fitness Val " + (int)(sa.getDistance() /*+ sa.getCoins()*/ )); 
				genotype.setFitnessValue( (int)(sa.getDistance()*1.5 +  sa.didMarioWin()*sa.getTimeLeft()/*+ sa.getCoins()*COIN_ALPHA*/ ) );
			}
				
		}
		catch ( TranscriberException e ) {
			logger.warn( "transcriber error: " + e.getMessage() );
			genotype.setFitnessValue( 1 );
		}
		genomeNum++; 
		//System.out.println("Next Genome" ); 
		
	}
	//System.out.println("Next Gen... " ); 
//	Iterator it2 = genotypes.iterator();
//
//	//most a fitness should be is one
//	while ( it2.hasNext() ) {	
//		Chromosome genotype = (Chromosome) it2.next();
//		genotype.setFitnessValue( genotype.getFitnessValue()/getMaxFitnessValue() );/**/
//	}
	
//	service.shutdown();
//	while (!service.isTerminated())
//	{
//		//System.out.println("Not Finished");
//	}
	
//	System.out.println("Finished the service");
	
	/*service.shutdown();
	try {
		service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
	} catch (InterruptedException e) {
	  
	}*/
}

/**
 * @param responses
 * @param minResponse
 * @param maxResponse
 * @return fitness based on error.
 * @see TargetFitnessFunction#evaluate(List)
 */
protected abstract int calculateErrorFitness( double[][] responses, double minResponse,
		double maxResponse );

/**
 * @return if response is within this range of the target, error is 0
 */
protected double getTargetRange() {
	return targetRange;
}

/**
 * @return sequence of stimuli activation patterns
 */
protected double[][] getStimuli() {
	return stimuli;
}

/**
 * @return sequence of target values
 */
protected double[][] getTargets() {
	return targets;
}

/**
 * @return maximum possible fitness value for this function
 */
public int getMaxFitnessValue() {
	return maxFitnessValue;
}

}
