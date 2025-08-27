import java.util.*;

/**
 * Generates patients for simulating a hospital ER
 */

public class PatientGenerator {
    //========Fields and Methods for Creating Patients =================================

    // Fields and methods for creating patients with random priorities and treatments.
    // These are static becasue they are used by the static  getNextPatient method.

    // fields for controlling the probabilities used in the Patient creation process.
    private static Random random = new Random();  //used for generating the random values.

    // fields used to make new Patients, whose values are set by the GUI in HospitalER,
    //   (which is why they are public rather than private.)
    public static int arrivalInterval = 5;   // new patient every 5 ticks, on average
    public static double probPri1 = 0.1; // 10% priority 1 patients
    public static double probPri2 = 0.2; // 20% priority 2 patients
    
    /**
     * Static method to get a new Patient
     * If no new patients arriving at this time, return null
     * If a new patient is arriving at this time,
     * then create and return the Patient
     */
    public static Patient getNextPatient(int time){
        if (time>1 && random.nextDouble()>1.0/arrivalInterval) { // no patient on this time tick
            return null;
        }
        else {
            double rnd = random.nextDouble();
            int priority =  (rnd < probPri1)? 1 : (rnd < (probPri1 + probPri2) ) ? 2 : 3;
            String firstName = GetRandomName(firstNames);
            String lastName = GetRandomName(lastNames);
            Queue<Treatment> treatments = GenerateRandomTreatments(priority);
            return new Patient(time, priority, firstName, lastName, treatments);
        }
    }

    /**
     * Method to control the random number generator to make experiments repeatable
     */
    public static void setRandomSeed(long seed){ random.setSeed(seed);}

    // getters and setters.
    public static int getArrivalInterval(){return arrivalInterval;}
    public static void setArrivalInterval(double v){arrivalInterval = (int)v;}

    public static int getProbPri1(){return (int)(probPri1*100);}
    public static void setProbPri1(double v){probPri1 = v/100;}

    public static int getProbPri2(){return (int)(probPri2*100);}
    public static void setProbPri2(double v){probPri2 = v/100;}


    // Creating random names and treatments
    /**
     * Create a queue of random treatments
     * The sequence is influenced by priority of the patient:
     *  - high priority patients are more likely to need the operating
     *    theatre first, and a more complicated treatment sequence.
     *  low priority patients are more likely to just need an ER bed treatment.
     */
    private static Queue<Treatment> GenerateRandomTreatments(int priority){
        //choose number of treatments.
        //choose location and length of each treatment
        //
        // always start with ER
        // if (random.nextDouble()<0.8 ||
        //     (priority==1 && random.nextDouble()<0.4)){
        Queue<Treatment> treatments = new ArrayDeque<Treatment>();
        treatments.offer(new Treatment("ER", GenerateTime(20)));
        String lastDept = "ER";

        //many high priority patients need surgery.
        if ((priority==1  && random.nextDouble()<0.4) ||
        (priority==2 && random.nextDouble()<0.1)){
            treatments.offer(new Treatment("Surgery", GenerateTime(60)));
            lastDept = "Surgery";

        }
        Treatment newTreatment;
        for (int i=random.nextInt(5); i>=0; i--){    // up to 5 more treatments
            do { // ensure that department is not the same as the previous department.
                double num = random.nextDouble();

                if (num<0.05)     {newTreatment = new Treatment("MRI", GenerateTime(200));}
                else if (num<0.1) {newTreatment = new Treatment("Surgery", GenerateTime(200));}
                else if (num<0.35){newTreatment = new Treatment("X-Ray", GenerateTime(20));}
                else if (num<0.6) {newTreatment = new Treatment("UltraSound", GenerateTime(20));}
                else {newTreatment = new Treatment("ER", GenerateTime(10));}
            } while (newTreatment.getDepartment()==lastDept);
            treatments.offer(newTreatment);
            lastDept = newTreatment.getDepartment();
        }
        return treatments;
    }

    /**
     * Generate a random time a patient will require for a treatment
     */
    public static int GenerateTime(int medianTime){
        double logNorm = Math.exp(0.6*random.nextGaussian());
        return (int)(1 + Math.max(0, medianTime-1)*logNorm);
    }

    /**
     * Create a random name for the Patient using the lists below
     */
    private static String GetRandomName(String[] names){
        return names[random.nextInt(names.length)];
    }

    private static String[] firstNames =
        {"Lisa","Ramon","Janet","Catherine","Chris","Wokje","Thuong","Andrea",
            "Manjeet","Toby","Philip","Bing","Renee","Derek","David","John",
            "Christian","Yongxin","Charles","Michael","Colin","Helen","Mansoor","Rod",
            "Todd","Dan","Colin","Shirley","Alex","John","Michael","Peter",
            "Paul","Ian","Jenny","Bob","Jeffrey","Joanna","Kathryn","Andy",
            "Inge","Maree","Rosie","Joanne","Yau","Rebecca","Robyn","Christine",
            "Guy","Christina","Tirta","Ruiping","Victoria","Bernadette","Catherine","Mo",
            "Tom","Natalie","Harold","Dimitrios","Alexander","James","Michael",
            "Yu-Wei","Emily","Christian","Alia","Zohar","Kimberly","Ocean","Yi",
            "Jamy","Travis","Deborah","Kim","Linda","Gillian","Bronwyn","Bruce",
            "Miriam","Gillian","Jenny"};

    private static String[] lastNames =
        {"Alcock","Ansell","Armstrong","Bai","Bates","Biddle","Bradley","Brunt","Calvert",
            "Chawner","Cho","Clark","Coxhead","Cullen","Daubs","Day","Dinica","Downey",
            "Dunbar","Elinoff","Fortune","Gabrakova","Geng","Goreham","Groves","Hall",
            "Harris","Hodis","Horgan","Hunt","Jackson","Jones","Keane-Tuala","Khaled",
            "Kidman","Krtalic","Laufer","Levi","Locke","Mackay","Marquez","Maskill",
            "Maxwell","McCrudden","McGuinness","McMillan","Mei","Millington","Moore",
            "Murphy","Nelson","Niemetz","O'Hare","Owen","Pearce","Perris","Pirini",
            "Pratt","Randal","Reilly","Rimoni","Robinson","Ruck","Schipper",
            "Servetto","Shuker","Skinner","Speedy","Stevens","Sweet","Taylor",
            "Terreni","Timperley","Turetsky","Vignaux","Wallace","Welch","Wilson",
            "Ackerley","Adds","Anderson","Anslow","Antunes","Armstrong","Arnedo-Gomez",
            "Bacharach","Bai","Barrett","Baskerville","Bennett","Berman","Boniface",
            "Boston","Brady","Bridgman","Brunt","Buettner","Calhoun","Calvert",
            "Capie","Carmel","Chiaroni","Chicca","Chu","Chu","Clark",
            "Clayton","Coxhead","Craig","Cuffe","Cullen","Dalli","Das",
            "Davidson","Davies","Desai","Devue","Dinneen","Dmochowski","Downey",
            "Doyle","Dumitrescu","Dunbar","Elgort","Elias","Faamanatu-Eteuati","Feld",
            "Fraser","Frean","Galvosas","Gamble","Geng","George","Goh",
            "Goreham","Gregory","Grener","Guy","Haggerty","Hammond","Hannah",
            "Harvey","Haywood","Hodis","Hogg","Horgan","Horgan","Hubbard",
            "Hui","Ingham","Jack","Johnston","Johnston","Jordan","Joyce",
            "Keane-Tuala","Kebbell","Keyzers","Khaled","Kiddle","Kiddle","Kirkby",
            "Knewstubb","Kuehne","Lacey","Leah","Leggott","Levi","Lindsay",
            "Loader","Locke","Lynch","Ma","Mallett","Mares","Marriott",
            "Marshall","Maslen","Mason","Maxwell","May","McCarthy","McCrudden",
            "McDonald","McGregor","McKee","McKinnon","McNeill","McRae","Mercier",
            "Metuarau","Millington","Mitsotakis","Molloy","Moore","Muaiava","Muckle",
            "Natali","Neha","Newton","Nguyen","Nisa","Noakes-Duncan",
            "Ok","Overton","Park","Parkinson","Penetito","Perkins","Petkov",
            "Pham","Pivac","Plank","Price","Raman","Rees","Reichenberger",
            "Riad","Rice-Davis","Ritchie","Robb","Rofe","Rook","Ruegg",
            "Schick","Scott","Seals","Sheffield","Shewan","Sim","Simpson",
            "Smaill","Smith","Spencer","Stern","Susilo","Sutherland","Tariquzzaman",
            "Tatum","Te Huia","Te Morenga","Thirkell-White","Thomas","Tokeley","Trundle",
            "Van Belle","Van Rij","Vowles","Vry","Ward","Warren","White",
            "Whittle","Wilson","Wilson","Wood","Yao","Yu","Zareei",
            "de Saxe","de Sylva","van der Meer", "Woods","Yates","Zhang","van Zijl"
        };
}

