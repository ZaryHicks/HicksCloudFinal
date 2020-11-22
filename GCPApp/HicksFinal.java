import java.io.*; 
import java.util.*; 

import org.apache.hadoop.io.Text; 
import org.apache.hadoop.io.IntWritable; 
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.conf.Configuration; 
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job; 
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat; 
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat; 
import org.apache.hadoop.util.GenericOptionsParser;

public class HicksFinal {

	// Posting Class created to hold all Posting data required
	public static class Posting implements Comparable<Posting>{
		public Integer id;
		public String folder;
		public String filename;
		public int count;
		
		Posting(Integer i, String fold, String fn, int c){
			id = i; folder = fold; filename = fn; count = c;
		}
		
		Posting(String s){
			String[] parts = s.split(",");
			id = Integer.parseInt(parts[0]);
			folder = parts[1];
			filename = parts[2];
			count = Integer.parseInt(parts[3]);
		}
		
		@Override
		public int compareTo(Posting p) {
			return this.id - p.id;
		}
		
		@Override
		public String toString() {
			return new String(id + "," + folder + "," + filename + "," + count);
		}
	}
	
	// Mapper class for constructing Inverted Indices
 	public static class IIMapper extends Mapper<Object, Text, Text, Text> {
		
 		// Text for folder and filename
 		private Text folder = new Text();
 		private Text filename = new Text();
 		
 		// Map for counting
 		private HashMap<String, Integer> map;
 
 		// Map for DocID
 		private HashMap<String, Integer> ids;
 		
 		// Stopwords
 		private HashSet<String> stopwords;
 		
 		@Override
 		public void setup(Context context) throws IOException, InterruptedException{ 
			map = new HashMap<String, Integer>();
			stopwords = new HashSet<String>();
			
			// I hope my selection of stopwords is good - from list at countwordsfree.com/stopwords with some omissions
	    	String[] arr = {"to", "the", "of", "a", "and", "an", "if", "but", "for", "or", "do", "in",
	    					"ll", "as", "at", "so", "i", "my", "on", "by", "d", "be", "is", "are", "it", "s",
	    					"than", "its", "t", "this", "his", "with", "there", "they", "he", "had", "has", "into",
	    					"them", "we", "did", "were", "she", "you", "now", "was", "that", "what", "have", "from",
	    					"one", "said", "all", "which", "him", "not", "her",  "made", "before", "where", "some",
	    					"our", "shall", "more", "thou", "being", "went", "upon", "well", "how", "go", "know",
	    					"come", "out", "their", "when", "who", "will", "no", "thee", "could", "see", "am", "like",
	    					"been", "up", "your", "me", "most", "away", "those", "still", "about", "should", "himself",
	    					"say", "let", "way", "look", "much", "down", "why", "only", "these", "very", "then", "nothing",
	    					"may", "other", "us", "such", "two", "here", "thy", "would", "once", "hath", "same", "over", "take",
	    					"any", "make", "nor", "yes", "yet", "without", "after", "little", "must", "o", "though", "never",
	    					"too", "can", "first", "put", "done", "de", "another", "every", "tell", "think", "words", "word",
	    					"something", "off", "give", "even", "again", "because", "thing", "seemed", "under", "m", "back",
	    					"many", "against", "through", "came", "own", "whom", "right", "tis", "having", "took", "mine", "while",
	    					"last", "just", "always", "both", "z", "www"};
	    	for(String s : arr) {
	    		stopwords.add(s);
	    	}
	    	
	    	// Since the provided documents we are using are predetermined, I will code them into be mapped and used for consistent docIds
	    	String[] docs = {"Miserables.txt", "NotreDame_De_Paris.txt", "anna_karenhina.txt", "war_and_peace.txt",
	    					 "allswellthatendswell", "asyoulikeit", "comedyoferrors", "cymbeline", "loveslabourslost",
	    					 "measureformeasure", "merchantofvenice", "merrywivesofwindsor", "midsummernightsdream",
	    					 "muchadoaboutnothing", "periclesprinceoftyre", "tamingoftheshrew", "tempest",
	    					 "troilusandcressida", "twelfthnight", "twogentlemenofverona", "winterstale",
	    					 "1kinghenryiv", "1kinghenryvi", "2kinghenryiv", "2kinghenryvi", "3kinghenryvi",
	    					 "kinghenryv", "kinghenryviii", "kingjohn", "kingrichardii", "kingrichardiii",
	    					 "loverscomplaint", "rapeoflucrece", "sonnets", "various", "venusandadonis",
	    					 "antonyandcleopatra", "coriolanus", "hamlet", "juliuscaesar", "kinglear", "macbeth",
	    					 "othello", "romeoandjuliet", "timonofathens", "titusandronicus"};
	    	// map for ids
	    	ids = new HashMap<String, Integer>();
	    	Integer id = 1;
	    	
	    	// add each document to the map, with an id
	    	for(String s : docs) {
	    		ids.put(s, id);
	    		id++;
	    	}
		} 
 		
 		@Override
		public void map(Object key, Text value, Context context) throws IOException,InterruptedException {
			// Clean the text and remove punctuation
 			String cleanLine = value.toString().toLowerCase().replaceAll("[_|$#<>\\^=\\[\\]\\*/\\\\,;,.\\-:()?!\"']", " ");
            StringTokenizer itr = new StringTokenizer(cleanLine);
 			
 			// from context, get the file name and path, split path based on /
 			String [] path = ((FileSplit) context.getInputSplit()).getPath().toString().split("/");
            
            // set filename
 			String fn = ((FileSplit) context.getInputSplit()).getPath().getName();
			filename.set(fn);
			
			// Use p to construct the folder path we want
			int fileLoc = 0;
			for(int i = 0; i < path.length; i++) {
				if(path[i].equals("files")) {
					fileLoc = i;
					break;
				}
			}
			// Find just the folder elements
			StringBuilder sb = new StringBuilder();
			sb.append("/");
			for(int i = fileLoc+1; i < path.length-1; i++) {
				sb.append(path[i] + "/");
			}
			folder.set(sb.toString());
			
			// for every word in the file (every token)
			while(itr.hasMoreTokens()) { 
				
				//String of this token
				String s = itr.nextToken();
			
				// ignore if stopword
				if(stopwords.contains(s)) {
					continue;
				}
				
				// get the Integer value from the map
				Integer count = map.get(s);
				
				// if p is null, there is no posting for this term yet, so we create 1
				if(count == null || count == (Integer) 0) {
					count = (Integer) 1;
				}
				else { // if p is not null, there is a posting for this term, so we jsut increase its count by 1
					count += 1;
				}
				
				// then we put it in the map (either for the first time, or updating the count);
				map.put(s, count);
			}
		}
 		
 		@Override
		public void cleanup(Context context) throws IOException, InterruptedException { 
 			// at this point, we have looked at every term in the doc, and made a posting with updated count for each
			for (Map.Entry<String, Integer> entry : map.entrySet()) { 		
 				Text k = new Text(entry.getKey());
 				StringBuilder sb = new StringBuilder();
 				Integer id = ids.get(filename.toString());
 				if(id == null) {
 					id = (Integer) 0;
 				}
 				sb.append(id.toString() + "," + folder.toString() + "," + filename.toString() + "," + entry.getValue().toString());
 				Text v = new Text(sb.toString());
 				context.write(k, v);
			}
		} 
		
	}
	
 	// Reducer for II
	public static class IIReducer extends Reducer<Text, Text, Text, Text> {
		
		// Custom comparator, to sort by docId
		private class idComp implements Comparator<Posting>{
			public int compare(Posting a, Posting b) {
				return a.id - b.id;
			}
		}
		
		// PQ will hold postings, and sort them by docId
		private PriorityQueue<Posting> pq;
		
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
			pq = new PriorityQueue<Posting>(new idComp());
			
			// for every posting in the list, add to the PQ, which sorts it by doc id
			for(Text t : values) {
				pq.add(new Posting(t.toString()));
			}
			
			// then use a string builder to accumulate all postings as text
			StringBuilder sb = new StringBuilder();
			
			// Append the postings for this term to the list
			while(!pq.isEmpty()) {
				Posting p = pq.poll();
				sb.append(p.toString() + " -> ");
			}
			
			// This will be our end char, end of this posting list
			sb.append("X");
			
			// write Term, Posting List
			context.write(key, new Text(sb.toString()));
		}
	}
 	
 	
	// Mapper for Top-N
	public static class TopNMapper extends Mapper<Object, Text, Text, IntWritable> { 
		
	    private Text word = new Text();
	    
	    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
	    	String cleanLine = value.toString().replaceAll("[_|$#<\\^=\\[\\]\\*/\\\\,;,.\\-:()?!\"']", " ");
            StringTokenizer itr = new StringTokenizer(cleanLine);
	    	
            // take all inverted index files form:
            // term	docId,Folder,Filename,Count -> ... -> X
            // cleanLine will remove all punctuation EXCEPT for > in order to know when count is
            
            // Loop logic
            boolean grabTerm = true;
            int count = 0;
            String prev = null;
            String curr = null;
            
            // Grab term, then look for >, when we see >, add the count. Continue until we see X, then emit, and grab next term
            while(itr.hasMoreTokens()) {
            	// hold current
            	curr = itr.nextToken();
            	
            	// when this is true, this token is our term
            	if(grabTerm) {
            		word.set(curr);
            		grabTerm = false;
            	}
            	
            	// if we reach a >, we must add prev (the count)
            	if(curr.equals(">")) {
            		count += Integer.parseInt(prev);
            	}
            	
            	// we have reached the end of this posting list, we will emit this and grab next
            	if(curr.equals("X")) {
            		grabTerm = true;
            		context.write(word, new IntWritable(count));
            		count = 0;
            	}
            	
            	// prev to curr every time
            	prev = curr;	
            }
	    }
	}
 	
	
	// Reducer for Top-N
	public static class TopNReducer extends Reducer<Text, IntWritable, Text, IntWritable> { 
		
		// Node to hold and sort the counts
		private class Node{
			public String term;
			public int count;
			
			public Node(String s, int c) {
				term = s;
				count = c;
			}
		}
		
		// Custom comparator, sort by Node.count
		private class nodeComp implements Comparator<Node>{
			public int compare(Node a, Node b) {
				return a.count - b.count;
			}
		}
		
		// TreeMap for storing/sorting least 5
		//private TreeMap<Integer, String> map2; 
		private PriorityQueue<Node> pq;
		
		@Override
		public void setup(Context context) throws IOException, InterruptedException{ 
			//map2 = new TreeMap<Integer, String>(new myComp());
			pq = new PriorityQueue<Node>(new nodeComp());
		} 

		@Override
		public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException { 
			
			// From config, we get N value - from command line (or from app)
			Configuration conf = context.getConfiguration();
			int n = Integer.parseInt(conf.get("N"));
			// Use N for our size bounds
			
			
			int sum = 0;
		    for (IntWritable val : values) {
		        sum += val.get();
		    }
		    
		    pq.add(new Node(key.toString(), sum));
		    
		    if(pq.size() > n) {
		    	pq.poll(); //removes head of pq (we may need to invert sort?)
		    }
		} 

		// For quick output
		private Text word = new Text();
		private IntWritable cnt = new IntWritable();
		
		@Override
		public void cleanup(Context context) throws IOException, InterruptedException { 
			while(!pq.isEmpty()) {
				Node n = pq.poll();
				word.set(n.term);
				cnt.set(n.count);
				context.write(word, cnt);
			}
		} 
	}
	
	
	// Mapper for Search Term
	public static class TermMapper extends Mapper<Object, Text, Text, Text> { 
		
	    private Text word = new Text();
	    private Text posting = new Text();
	    
	    public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
	    	String cleanLine = value.toString().replaceAll("[|$#<\\^=\\[\\]\\*\\\\;\\-:()?!\"']", " ");
            StringTokenizer itr = new StringTokenizer(cleanLine);
	    	
            // From config, we get the TERM
 			Configuration conf = context.getConfiguration();
 			String term = conf.get("term").toLowerCase();
    
            // Loop logic
            boolean grabTerm = true;
            boolean match = false;
            String prev = null;
            String curr = null;
            
            StringBuilder sb = new StringBuilder();
            
            // Grab term, then look for >, when we see >, add the count. Continue until we see X, then emit, and grab next term
            while(itr.hasMoreTokens()) {
            	// hold current
            	curr = itr.nextToken();
            	
            	// when this is true, this token is our term
            	if(grabTerm) {
            		word.set(curr);
            		grabTerm = false;
            		
            		// check to see if the word we grabbed matches our desired term
            		if(word.toString().equals(term)) {
            			// if it does, set match
            			match = true;
            		}
            		else {
            			match = false;
            		}
            	}
            	
            	// if match is true, word holds our desired term, we must now get the postings for this word
            	if(match) {
            		
            		// if we reach a >, prev is a posting for this term, so we add it to the output
                	if(curr.equals(">")) {
                		sb.append(prev + " -> ");
                	}
            		
                	// we have reached the end of this posting list, now emit the entire list, and we must now grab a new term
                	if(curr.equals("X")) {
                		grabTerm = true;
                		match = false;
                		posting.set(sb.toString());
                		context.write(word, posting);
                	}	
            	}
            	
            	// prev to curr every time
            	prev = curr;	
            }
	    }
	}
	 	
		
	// Reducer for Search Term
	public static class TermReducer extends Reducer<Text, Text, Text, Text> { 
		
		@Override
		public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException { 

			// We should get just one key and a list of all the postings
			StringBuilder postingList = new StringBuilder();
			
			// For every Posting in text form from values, add it to our list
			for(Text t : values) {
				postingList.append(t.toString());
			}
			postingList.append("X");
			
			context.write(key, new Text(postingList.toString()));
		} 
	}
	
	// Main method, this in invoked upon calling the program
	public static void main(String[] args) throws Exception { 
        Configuration conf = new Configuration(); 
        String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs(); 
  
        // if less than two paths  
        // provided will show error 
        if (otherArgs.length < 2){ 
            System.err.println("Error: please provide the two arguments needed"); 
            System.exit(2); 
        } 
  
        // FULL FORM ON SSH 
        // hadoop jar HicksFinal.jar HicksFinal -type [value]
        // type { -i, -t, -n} for Inverted, Term, Top-N
        // value { HTS, term, N} for InputFiles, term to search, N to use
        
        // grab the type
        String type = otherArgs[1];
        
        //Create Job reference
        Job job = Job.getInstance(conf, "temp"); ;
        
        // we will time each operation in ms and print out after completion
        long startTime = System.currentTimeMillis();
        
        if(type.equals("-i")) { // Construct Inverted Index =======================
            // Function form:
        	// hadoop jar HicksFinal.jar HicksFinal -i [HTS]
            
        	// inputs holds H, T, S, or a combination
        	String inputs = otherArgs[2];
        	boolean h = false, t = false, s = false;
        	if(inputs.contains("H") || inputs.contains("h")) {
        		h = true;
        	}
        	if(inputs.contains("T") || inputs.contains("t")) {
        		t = true;
        	}
        	if(inputs.contains("S") || inputs.contains("s")) {
        		s = true;
        	}        	
        	// if they are all false, error
        	if(!h && !t && !s) {
        		System.err.println("Error: provide input file arguments some combination of HTS"); 
                System.exit(2); 
        	}
        	
        	// Setup job for this function
            job = Job.getInstance(conf, "Inverted Index"); 
            job.setJarByClass(HicksFinal.class);
      
            // Mapper and Reducer for II
            job.setMapperClass(IIMapper.class);
            job.setReducerClass(IIReducer.class); 

            // Outputs Text,Text
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            
            // Determine and add input paths
            if(h) { //Include Hugo
            	FileInputFormat.addInputPath(job, new Path("/user/hdfs/files/Hugo/")); 
            }
            if(t) { //Include Tolstoy
            	FileInputFormat.addInputPath(job, new Path("/user/hdfs/files/Tolstoy/")); 
            }
            if(s) { //Include Shakespeare
            	FileInputFormat.addInputPath(job, new Path("/user/hdfs/files/Shakespeare/comedies"));
            	FileInputFormat.addInputPath(job, new Path("/user/hdfs/files/Shakespeare/histories"));
            	FileInputFormat.addInputPath(job, new Path("/user/hdfs/files/Shakespeare/poetry"));
            	FileInputFormat.addInputPath(job, new Path("/user/hdfs/files/Shakespeare/tragedies"));
            }
            // Output Inverted Indices to this location (since it is once per run of app)
            FileOutputFormat.setOutputPath(job, new Path("/user/hdfs/invertedind/")); 
        } // End -i
        else if(type.equals("-t")) { // Search for Term ===========================
        	// Function Form:
            // hadoop jar HicksFinal.jar HicksFinal -t term
            
        	// term holds the word to search for, passed into job config
        	conf.set("term", otherArgs[2]);
        	
        	// Setup Job for Term Search
            job = Job.getInstance(conf, "Term Search"); 
            job.setJarByClass(HicksFinal.class);
            
            // One task for this because we only have 1 key (and thus only need one reducer)
            job.setNumReduceTasks(1);
      
            // Set Mapper and Reducer (CHANGE)
            job.setMapperClass(TermMapper.class);
            job.setReducerClass(TermReducer.class); 

            // Output Text, Text
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(Text.class);
            
            // Input is From Inverted Indices
            FileInputFormat.addInputPath(job, new Path("/user/hdfs/invertedind/")); 
            
            // Output to a folder for this specific term
            FileOutputFormat.setOutputPath(job, new Path("/user/hdfs/term/" + otherArgs[2] + "/")); 
        } // end term search
        else if(type.equals("-n")) { // Search For Top-N Occurrences =================
        	// Function form:
            // hadoop jar HicksFinal.jar HicksFinal -n N
        	
        	// n holds the argument for top-n, it is passed into job configuration
        	conf.set("N", otherArgs[2]);
        	
        	// Setup Job for Top-N
            job = Job.getInstance(conf, "Top-N"); 
            job.setJarByClass(HicksFinal.class);
            
            // One Reduce task means we find the top-N over all Indices and output 1 file
            job.setNumReduceTasks(1);
      
            // Set Mapper and Reducer
            job.setMapperClass(TopNMapper.class);
            job.setReducerClass(TopNReducer.class); 

            // Output Text, Int (Term, Freq)
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);
            
            // Input is From Inverted Indices
            FileInputFormat.addInputPath(job, new Path("/user/hdfs/invertedind/")); 
            
            // Output to a folder for this specific top-N value
            FileOutputFormat.setOutputPath(job, new Path("/user/hdfs/topN/" + otherArgs[2] + "/")); 
        } // end top-n
        else {
        	System.err.println("Error: Invalid Argument provided. -i INVERTED INDEX, -t TERM SEARCH, -n TOP-N");
        }
        
        // Submit job and wait for completion
        boolean exitCode = job.waitForCompletion(true);
        
        // record the end time and print it
        long endTime = System.currentTimeMillis();
        System.out.println("Executed in: " + (endTime - startTime) + "ms");
        
        // Exit
        System.exit(exitCode ? 0 : 1);
    } 
}
