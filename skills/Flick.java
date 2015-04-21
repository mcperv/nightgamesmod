package skills;

import global.Global;
import characters.Attribute;
import characters.Character;
import characters.Emotion;
import characters.Trait;

import combat.Combat;
import combat.Result;

public class Flick extends Skill {

	public Flick(Character self) {
		super("Flick", self);
	}

	@Override
	public boolean requirements() {
		return self.getPure(Attribute.Seduction)>=17 && !self.has(Trait.softheart);
	}

	@Override
	public boolean usable(Combat c, Character target) {
		return target.pantsless()&&c.getStance().reachBottom(self)&&self.canAct()&&!c.getStance().penetration(target)&&!self.has(Trait.shy);
	}

	@Override
	public void resolve(Combat c, Character target) {
		if(target.roll(this, c, accuracy()+self.tohit())){
			int m = Global.random(6)+5;
			if(self.human()){
				c.write(self,deal(c,m,Result.normal, target));
			}
			else if(target.human()){
				c.write(self,receive(c,m,Result.normal, target));
			}
			if(target.has(Trait.achilles)){
				m+=2+Global.random(target.get(Attribute.Perception)/2);
			}
			target.pain(c, m);
			target.modMojo(-15);
			self.buildMojo(15);
			self.emote(Emotion.dominant, 10);
			target.emote(Emotion.angry,15);
			target.emote(Emotion.nervous,15);
		}
		else{
			if(self.human()){
				c.write(self,deal(c,0,Result.miss, target));
			}
			else if(target.human()){
				c.write(self,receive(c,0,Result.miss, target));
			}
		}
	}

	@Override
	public boolean requirements(Character user) {
		return user.getPure(Attribute.Seduction)>=17 && !user.has(Trait.softheart);
	}

	@Override
	public Skill copy(Character user) {
		return new Flick(user);
	}
	public int speed(){
		return 6;
	}
	public Tactics type(Combat c) {
		return Tactics.damage;
	}

	@Override
	public String deal(Combat c, int damage, Result modifier, Character target) {
		if(modifier==Result.miss){
			return "You flick your finger between "+target.name()+"'s legs, but don't hit anything sensitive.";
		}
		else{
			if(target.hasBalls()){
				return "You use two fingers to simultaneously flick both of "+target.name()+" dangling balls. She tries to stifle a yelp and jerks her hips away reflexively. " +
						"You feel a twing of empathy, but she's done far worse.";
			}
			else{
				return "You flick your finger sharply across "+target.name()+"'s sensitive clit, causing her to yelp in surprise and pain. She quickly covers her girl parts " +
						"and glares at you in indignation.";
			}
		}
	}

	@Override
	public String receive(Combat c, int damage, Result modifier, Character target) {
		if(modifier==Result.miss){
			return self.name()+" flicks at your balls, but hits only air.";
		}
		else{
			return self.name()+" gives you a mischievous grin and flicks each of your balls with her finger. It startles you more than anything, but it does hurt and " +
				"her seemlingly carefree abuse of your jewels destroys your confidence.";
		}
	}

	@Override
	public String describe() {
		return "Flick opponent's genitals, which is painful and embarrassing";
	}

	@Override
	public boolean makesContact() {
		return true;
	}
}
