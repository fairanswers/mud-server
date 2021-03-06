package mud.weather;

/**
 * WeatherState.java
 * 
 * Represents a state of weather, such as clouds, rain, snow, or a combination of them,
 * as well as the likelihood of the weather getting "worse".
 * 
 * This is used to implement a weather system of sorts inspired by the ideas here:
 * http://textgaming.blogspot.com/2011/10/weather-ii.html
 * 
 * @author Jeremy Harton
 *
 */
public class WeatherState
{
	private String name;                     // name/type of weather state
	private String description;              // description of the weather state
	public String transDownText;             // the text describing the weather shift if it transitioned down to this
	public String transUpText;               // the text describing the weather shift if it transitions up to this
	
	public double TransitionDownProbability; // How likely the weather is to transition "down a state" (worse weather)
	public boolean Precipitation;            // Is precipitation possible? (true/false)
	public boolean Wind;                     // Is wind a possibility? (true/false)
	public boolean Clouds;                   // Are clouds a possibility? (true/false)
	public boolean Storm;                    // Is a storm possible? (true/false)
	
	public int upDown = 0;                   // indicator of in which direction (up/down) the weather transitioned, if it changed
	
	/*public WeatherState(double tdp, boolean precip, boolean wind, boolean clouds, boolean storm) {
		this.TransitionDownProbability = tdp;
		this.Precipitation = precip;
		this.Wind = wind;
		this.Clouds = clouds;
		this.Storm = storm;
	}*/
	
	public WeatherState(String name, double tdp, boolean precip, boolean wind, boolean clouds, boolean storm) {
		this.name = name;
		
		this.TransitionDownProbability = tdp;
		
		this.Precipitation = precip;
		
		this.Wind = wind;
		this.Clouds = clouds;
		this.Storm = storm;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(final String newDescription) {
		this.description = newDescription;
	}
	
	public double getTransDownProb() {
		return this.TransitionDownProbability;
	}
	
	public String toString() {
		return this.TransitionDownProbability + " " + this.Precipitation + " " + this.Wind + " " + this.Clouds +  " " + this.Storm;
	}
}