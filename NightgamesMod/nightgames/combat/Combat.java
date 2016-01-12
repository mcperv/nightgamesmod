package nightgames.combat;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import nightgames.areas.Area;
import nightgames.characters.Attribute;
import nightgames.characters.Character;
import nightgames.characters.Emotion;
import nightgames.characters.NPC;
import nightgames.characters.State;
import nightgames.characters.Trait;
import nightgames.characters.body.BodyPart;
import nightgames.characters.body.BodyPartMod;
import nightgames.characters.body.PussyPart;
import nightgames.global.DebugFlags;
import nightgames.global.Flag;
import nightgames.global.Global;
import nightgames.items.Item;
import nightgames.pet.Pet;
import nightgames.skills.Anilingus;
import nightgames.skills.BreastWorship;
import nightgames.skills.CockWorship;
import nightgames.skills.FootWorship;
import nightgames.skills.PussyWorship;
import nightgames.skills.Skill;
import nightgames.stance.Neutral;
import nightgames.stance.Position;
import nightgames.stance.Stance;
import nightgames.stance.StandingOver;
import nightgames.status.Braced;
import nightgames.status.CounterStatus;
import nightgames.status.Stsflag;
import nightgames.status.Wary;
import nightgames.status.Winded;
import nightgames.stance.Engulfed;

public class Combat extends Observable implements Serializable, Cloneable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8279523341570263846L;
	public Character p1;
	public CombatantData p1Data;
	public Character p2;
	public CombatantData p2Data;
	public int phase;
	private Skill p1act;
	private Skill p2act;
	public Area location;
	private String message;
	private Position stance;
	public Character lastTalked;
	private int timer;
	public Result state;
	private HashMap<String,String> images;
	String imagePath = "";

	public Combat(Character p1, Character p2, Area loc){
		this.p1=p1;
		p1Data = new CombatantData();
		this.p2=p2;
		p2Data = new CombatantData();
		p1.startBattle(this);
		p2.startBattle(this);
		location=loc;
		stance = new Neutral(p1,p2);
		message = "";
		timer=0;
		images = new HashMap<String,String>();
		p1.state=State.combat;
		p2.state=State.combat;
	}
	public Combat(Character p1, Character p2, Area loc, Position starting){
		this(p1,p2,loc);
		stance = starting;
	}
	public Combat(Character p1, Character p2, Area loc,int code){
		this(p1,p2,loc);
		stance = new Neutral(p1,p2);
		message = "";
		timer=0;
		switch(code){
			case 1:
				p2.undress(this);
				p1.emote(Emotion.dominant, 50);
				p2.emote(Emotion.nervous, 50);
			default:
		}
		p1.state=State.combat;
		p2.state=State.combat;
	}
	public void go(){
		phase=0;
		if(p1.mostlyNude() && !p2.mostlyNude()){
			p1.emote(Emotion.nervous, 20);
		}
		if(p2.mostlyNude() && !p1.mostlyNude()){
			p2.emote(Emotion.nervous, 20);
		}
		if(!(p1.human()||p2.human())){
			automate();
		}
		this.updateMessage();
	}
	
	public CombatantData getCombatantData(Character character) {
		if (character == p1) {
			return p1Data;
		} else if (character == p2) {
			return p2Data;
		} else {
			throw new IllegalArgumentException(character + " is not in combat " + this);
		}
	}
	
	private boolean checkBottleCollection(Character victor, Character loser, PussyPart mod) {
		return victor.has(Item.EmptyBottle, 1) && loser.body.get("pussy").stream().anyMatch(part -> part.getMod() == mod);
	}

	public void doVictory(Character victor, Character loser) {
		if (loser.hasDick() && victor.has(Trait.succubus)) {
			victor.gain(Item.semen, 3);
			if (loser.human()) {
				write(victor, "<br><b>As she leaves, you see all your scattered semen ooze out and gather into a orb in " + victor.nameOrPossessivePronoun() + " hands. " +
						"She casually drops your seed in some empty vials that appeared out of nowhere</b>");
			} else if (victor.human()) {
				write(victor, "<br><b>" + loser.nameOrPossessivePronoun() + " scattered semen lazily oozes into a few magically conjured flasks. " +
						"To speed up the process, you milk " + loser.possessivePronoun() + " out of the last drops " +
						loser.subject() + " had to offer. Yum, you just got some leftovers.</b>");
			}
		} else if (loser.hasDick() && (victor.human() || victor.has(Trait.madscientist)) && victor.has(Item.EmptyBottle, 1)) {
			// for now only the player and mara collects semen
			write(victor, Global.format("<br><b>{self:SUBJECT-ACTION:manage|manages} to collect some of {other:name-possessive} scattered semen in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.semen, 1);
		}
		if (checkBottleCollection(victor, loser, PussyPart.divine)) {
			write(victor, Global.format("<br><b>{other:SUBJECT-ACTION:shoot|shoots} {self:name-do} a dirty look as {self:subject-action:move|moves} to collect some of {other:name-possessive} divine pussy juices in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.HolyWater, 1);
		}
		if (checkBottleCollection(victor, loser, PussyPart.succubus)) {
			write(victor, Global.format("<br><b>{other:SUBJECT-ACTION:shoot|shoots} {self:name-do} a dirty look as {self:subject-action:move|moves} to collect some of {other:name-possessive} demonic pussy juices in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.ExtremeAphrodisiac, 1);
		}
		if (checkBottleCollection(victor, loser, PussyPart.plant)) {
			write(victor, Global.format("<br><b>{other:SUBJECT-ACTION:shoot|shoots} {self:name-do} a dirty look as {self:subject-action:move|moves} to collect some of {other:possessive} nectar in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.nectar, 3);
		}
		if (checkBottleCollection(victor, loser, PussyPart.cybernetic)) {
			write(victor, Global.format("<br><b>{other:SUBJECT-ACTION:shoot|shoots} {self:name-do} a dirty look as {self:subject-action:move|moves} to collect some of {other:possessive} artificial lubricant in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.LubricatingOils, 1);
		}
		if (checkBottleCollection(victor, loser, PussyPart.arcane)) {
			write(victor, Global.format("<br><b>{other:SUBJECT-ACTION:shoot|shoots} {self:name-do} a dirty look as {self:subject-action:move|moves} to collect some of the floating mana wisps ejected from {other:possessive} orgasm in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.RawAether, 1);
		}
		if (checkBottleCollection(victor, loser, PussyPart.feral)) {
			write(victor, Global.format("<br><b>{other:SUBJECT-ACTION:shoot|shoots} {self:name-do} a dirty look as {self:subject-action:move|moves} to collect some of {other:possessive} musky juices in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.FeralMusk, 1);
		}
		if (checkBottleCollection(victor, loser, PussyPart.gooey)) {
			write(victor, Global.format("<br><b>{other:SUBJECT-ACTION:shoot|shoots} {self:name-do} a dirty look as {self:subject-action:move|moves} to collect some of {other:possessive} goo in an empty bottle</b>", victor, loser));
			victor.consume(Item.EmptyBottle, 1, false);
			victor.gain(Item.BioGel, 1);
		}
		if (loser.human()) {
			write("<br>Ashamed at your loss, you resolve to win next time.");
			write("<br><b>Gained 1 Willpower</b>.");
			loser.getWillpower().gain(1);
		}
		victor.getWillpower().fill();
		loser.getWillpower().fill();
	}

	public void turn(){
		if(p1.checkLoss()&&p2.checkLoss()){
			state = eval();
			p1.evalChallenges(this, null);
			p2.evalChallenges(this, null);
			p2.draw(this,state);
			phase=2;
			this.updateMessage();
			if(!(p1.human()||p2.human())){
				end();
			}
			return;
		}
		if(p1.checkLoss()){
			state = eval();
			p1.evalChallenges(this, p2);
			p2.evalChallenges(this, p2);
			p2.victory(this,state);
			doVictory(p2,p1);
			phase=2;
			this.updateMessage();
			if(!(p1.human()||p2.human())){
				end();
			}
			return;
		}
		if(p2.checkLoss()){
			state = eval();
			p1.evalChallenges(this, p1);
			p2.evalChallenges(this, p1);
			p1.victory(this,state);
			doVictory(p1,p2);
			phase=2;
			this.updateMessage();
			if(!(p1.human()||p2.human())){
				end();
			}
			return;
		}
		if(!p1.human()&&!p2.human()&&timer>15){
			if(p1.getArousal().get()>p2.getArousal().get()){
				state = eval();
				if(Global.isDebugOn(DebugFlags.DEBUG_SCENE)){
					System.out.println(p2.name()+" victory over "+p1.name());
				}
				p2.victory(this,state);
				doVictory(p2,p1);
				phase=2;
				this.updateMessage();
				if(!(p1.human()||p2.human())){
					end();
				}
				return;
			}
			else if(p1.getArousal().get()<p2.getArousal().get()){
				state = eval();
				if(Global.isDebugOn(DebugFlags.DEBUG_SCENE)){
					System.out.println(p1.name()+" victory over "+p2.name());
				}
				p1.victory(this,state);
				doVictory(p1,p2);
				phase=2;
				this.updateMessage();
				if(!(p2.human()||p1.human())){
					end();
				}
				return;
			}
			else{
				state = eval();
				if(Global.isDebugOn(DebugFlags.DEBUG_SCENE)){
					System.out.println(p2.name()+" draw with "+p1.name());
				}
				p2.draw(this,state);
				phase=2;
				this.updateMessage();
				if(!(p1.human()||p2.human())){
					end();
				}
				return;
			}
		}
		Character player;
		Character other;
		if (p1.human()) {
			player = p1;
			other = p2;
		} else {
			player = p2;
			other = p1;
		}
		phase=1;
		p1.regen(this);
		p2.regen(this);
		message = other.describe(player.get(Attribute.Perception), this)+"<p>"+Global.capitalizeFirstLetter(getStance().describe())+"<p>"+player.describe(other.get(Attribute.Perception), this)+"<p>";
		if((p1.human()||p2.human())&&!Global.checkFlag(Flag.noimage)){
			Global.gui().clearImage();
			Global.gui().displayImage(imagePath,images.get(imagePath));
		}
		p1act=null;
		p2act=null;
		p1.act(this);
		this.updateAndClearMessage();
	}
	
	private Result eval() {
		if(getStance().bottom.human() && getStance().inserted(getStance().top) && getStance().en == Stance.anal) {
			return Result.anal;
		}
		else if(getStance().inserted()){
			return Result.intercourse;
		}
		else{
			return Result.normal;
		}
	}
	
	Skill worshipSkills[] = {
		new BreastWorship(null),
		new CockWorship(null),
		new FootWorship(null),
		new PussyWorship(null),
		new Anilingus(null),
	};

	public boolean combatMessageChanged;

	private Skill checkWorship(Character self, Character other, Skill def) {
		if (other.has(Trait.objectOfWorship) && (other.breastsAvailable() || other.crotchAvailable())) {
			int chance = Math.min(20, Math.max(5, other.get(Attribute.Divinity) + 10 - self.getLevel()));
			if (Global.random(100) < chance) {
				List<Skill> avail = new ArrayList<Skill>(Arrays.asList(worshipSkills));
				Collections.shuffle(avail);
				while (!avail.isEmpty()) {
					Skill skill = avail.remove(avail.size() - 1).copy(self);
					if (Skill.skillIsUsable(this, skill, other)) {
						write(other, Global.format("<b>{other:NAME-POSSESSIVE} divine aura forces {self:subject} to forget what {self:pronoun} {self:action:were|was} doing and crawl to {other:direct-object} on {self:possessive} knees.</b>", self, other));
						return skill;
					}
				}
			}
		}
		return def;
	}

	public void act(Character c, Skill action, String choice){
		if(c==p1){
			p1act=action;
		}
		if(c==p2){
			p2act=action;
		}
		action.choice = choice;
		if (p1act==null) {
			p1.act(this);
		} else if (p2act==null) {
			p2.act(this);
		} else if(p1act!=null&&p2act!=null){
			clear();
			if (p1.human() || p2.human()) {
				Global.gui().clearText();
			}
			p1act = checkWorship(p1, p2, p1act);
			p2act = checkWorship(p2, p1, p2act);
			if(Global.isDebugOn(DebugFlags.DEBUG_SCENE)){
				System.out.println(p1.name()+" uses "+p1act.getLabel(this));
				System.out.println(p2.name()+" uses "+p2act.getLabel(this));
			}
			if(p1.pet!=null&&p2.pet!=null){
				petbattle(p1.pet,p2.pet);
			}
			else if(p1.pet!=null){
				p1.pet.act(this, p2);
			}
			else if(p2.pet!=null){
				p2.pet.act(this, p1);
			}
			useSkills();
			this.write("<br>");
			p1.eot(this,p2,p2act);
			p2.eot(this,p1,p1act);
			checkStamina(p1);
			checkStamina(p2);
			getStance().decay(this);
			getStance().checkOngoing(this);
			this.phase=0;
			if(!(p1.human()||p2.human())){
				timer++;
				turn();
			}
			this.updateMessage();
		}
	}
	public void automate(){
		int turn = 0;
		while(!(p1.checkLoss()||p2.checkLoss())){
			// guarantee the fight finishes in a timely manner
			if (turn > 50) {
				p1.pleasure(turn - 50, null);
				p1.pleasure(turn - 50, null);
			}
			turn += 1;
			phase=1;
			p1.regen(this);
			p2.regen(this);
			p1act=((NPC)p1).actFast(this);
			p2act=((NPC)p2).actFast(this);
			clear();
			if(Global.isDebugOn(DebugFlags.DEBUG_SCENE)){
				System.out.println(p1.name()+" uses "+p1act.getLabel(this));
				System.out.println(p2.name()+" uses "+p2act.getLabel(this));
			}
			if(p1.pet!=null&&p2.pet!=null){
				petbattle(p1.pet,p2.pet);
			}
			else if(p1.pet!=null){
				p1.pet.act(this, p2);
			}
			else if(p2.pet!=null){
				p2.pet.act(this, p1);
			}
			useSkills();
			p1.eot(this,p2,p2act);
			p2.eot(this,p1,p1act);
			checkStamina(p1);
			checkStamina(p2);
			getStance().decay(this);
			getStance().checkOngoing(this);
			this.phase=0;
		}
		if(p1.checkLoss()&&p2.checkLoss()){		
			state = eval();
			p1.evalChallenges(this, null);
			p2.evalChallenges(this, null);
			p2.draw(this,state);
			end();
			return;
		}
		if(p1.checkLoss()){
			state = eval();
			p1.evalChallenges(this, p2);
			p2.evalChallenges(this, p2);
			p2.victory(this,state);
			doVictory(p2,p1);
			end();
			return;
		}
		if(p2.checkLoss()){
			state = eval();
			p1.evalChallenges(this, p1);
			p2.evalChallenges(this, p1);
			p1.victory(this,state);
			doVictory(p1,p2);
			end();
			return;
		}
	}

	private boolean checkCounter(Character attacker, Character target, Skill skill) {
		return !target.has(Trait.submissive)&& getStance().mobile(target) && target.counterChance(this, attacker, skill) > Global.random(100);
	}

	private boolean resolveSkill(Skill skill, Character target) {
		boolean orgasmed = false;
		if(Skill.skillIsUsable(this, skill, target)){
			write(skill.user().subjectAction("use ", "uses ") + skill.getLabel(this) + ".");
			if (skill.makesContact() && !getStance().dom(target) && target.canAct() && checkCounter(skill.user(), target, skill)) {
				write("Countered!");
				target.counterattack(skill.user(), skill.type(this), this);
			} else if (target.is(Stsflag.counter) && skill.makesContact()) {
				write("Countered!");
				CounterStatus s = (CounterStatus)target.getStatus(Stsflag.counter);
				if (skill.user().is(Stsflag.wary)) {
					write(target, s.getCounterSkill().getBlockedString(this, skill.user()));
				} else {
					s.resolveSkill(this, skill.user());
				}
			} else {
				Skill.resolve(skill, this, target);
			}
			checkStamina(target);
			checkStamina(skill.user());
			orgasmed = checkOrgasm(skill.user(), target, skill);
		} else {
			write(skill.user().possessivePronoun() + " " + skill.getLabel(this) + " failed.");
		}
		return orgasmed;
	}

	private boolean checkOrgasm(Character user, Character target, Skill skill) {
		boolean orgasmed = false;
		if (target.checkOrgasm()) {
			target.doOrgasm(this, user, skill);
			orgasmed = true;
		}
		if (user.checkOrgasm()) {
			user.doOrgasm(this, target, skill);
			orgasmed = true;
		}
		return orgasmed;
	}
	private void useSkills() {
		Skill firstSkill, secondSkill;
		Character firstCharacter, secondCharacter;
		if(p1.init()+p1act.speed()>=p2.init()+p2act.speed()){
			firstSkill = p1act;
			secondSkill = p2act;
			firstCharacter = p1;
			secondCharacter = p2;
		} else {
			firstSkill = p2act;
			secondSkill = p1act;
			firstCharacter = p2;
			secondCharacter = p1;
		}
		if (!resolveSkill(firstSkill, secondCharacter)) {
			// only use second skill if an orgasm didn't happen
			write("<br>");
			resolveSkill(secondSkill, firstCharacter);
		}
	}

	public void clear(){
		message="";
		this.updateMessage();
	}

	public void write(String text){
		text = Global.capitalizeFirstLetter(text);
		if (text.isEmpty()) { return; } 
		String added=message+"<br>"+text;
		message = added;
		lastTalked = null;
	}

	public void updateMessage() {
		combatMessageChanged = true;
		this.setChanged();
		this.notifyObservers();
		this.setChanged();
	}

	public void updateAndClearMessage() {
		Global.gui().clearText();
		combatMessageChanged = true;
		this.setChanged();
		this.notifyObservers();
	}

	public void write(Character user, String text) {
		text = Global.capitalizeFirstLetter(text);
		if (text.length() > 0) {
			if(user.human()) {
				message=message+"<br><font color='rgb(200,200,255)'>"+text+"<font color='white'>";
			} else {
				message=message+"<br><font color='rgb(255,200,200)'>"+text+"<font color='white'>";
			}
			lastTalked = user;
		}
	}
	public String getMessage(){
		return message;
	}
	public String debugMessage() {
		return "Stance: " + this.getStance().getClass().getName()
				+ "\np1: " + p1.debugMessage(this, getStance())
				+ "\np2: " + p2.debugMessage(this, getStance());
	}
	public void checkStamina(Character p){
		if(p.getStamina().isEmpty() && !p.is(Stsflag.stunned)){
			p.add(this, new Winded(p));
			if(!getStance().prone(p)){
				Character other;
				if(p==p1){
					other=p2;
				}
				else{
					other=p1;
				}
				if(getStance().inserted()&&getStance().dom(other)){
					if(p.human()){
						write("Your legs give out, but "+other.name()+" holds you up.");
					}
					else{
						write(p.name()+" slumps in your arms, but you support her to keep her from collapsing.");
					}
				}
				else{
					setStance(new StandingOver(other,p));
					if(p.human()){
						write("You don't have the strength to stay on your feet. You slump to the floor.");
					}
					else{
						write(p.name()+" drops to the floor, exhausted.");
					}
				}
				p.loseWillpower(this, Math.min(p.getWillpower().max() / 8, 15), true);
			}
		}
	}

	public void next(){
		if(phase==0){
			turn();
		}
		else if(phase==2){
			end();
		}
	}
	public void intervene(Character intruder, Character assist){
		Character target;
		if(p1==assist){
			target=p2;
		}
		else{
			target=p1;
		}
		intruder.intervene3p(this, target, assist);
		assist.victory3p(this, target,intruder);
		phase=2;
		if(!(p1.human()||p2.human()||intruder.human())){
			end();
		}
		else if(intruder.human()){
			Global.gui().watchCombat(this);
		}
		this.updateMessage();
	}
	public boolean end(){
		clear();
		p1.state=State.ready;
		p2.state=State.ready;
		p1.endofbattle();
		p2.endofbattle();
		location.endEncounter();
		boolean ding=false;
		while (p1.getXP()>=p1.getXPReqToNextLevel()){
			p1.loseXP(p1.getXPReqToNextLevel());
			p1.ding();
			if(p1.human()){
				ding=true;
			}
		}
		while (p2.getXP()>=p2.getXPReqToNextLevel()){
			p2.loseXP(p2.getXPReqToNextLevel());
			p2.ding();
			if(p2.human()){
				ding=true;
			}
		}
		return ding;
	}
	public void petbattle(Pet one, Pet two){
		int roll1=Global.random(20)+one.power();
		int roll2=Global.random(20)+two.power();
		if(one.hasPussy()&&two.hasDick()){
			roll1+=3;
		}
		else if(one.hasDick()&&two.hasPussy()){
			roll2+=3;
		}
		if(roll1>roll2){
			one.vanquish(this,two);
		}
		else if(roll2>roll1) {
			two.vanquish(this,one);
		}
		else{
			write(one.own()+one+" and "+two.own()+two+" engage each other for awhile, but neither can gain the upper hand.");
		}
	}
	public Combat clone() throws CloneNotSupportedException {
	     Combat c = (Combat) super.clone();
         c.p1 = (Character) p1.clone();
         c.p2 = (Character) p2.clone();
         c.p1.finishClone(c.p2);
         c.p2.finishClone(c.p1);
         c.p1Data = (CombatantData) p1Data.clone();
         c.p2Data = (CombatantData) p2Data.clone();
         c.stance = (Position) getStance().clone();
         c.state = state;
         if (c.getStance().top == p1) c.getStance().top = c.p1;
         if (c.getStance().top == p2) c.getStance().top = c.p2;
         if (c.getStance().bottom == p1) c.getStance().bottom = c.p1;
         if (c.getStance().bottom == p2) c.getStance().bottom = c.p2;
         return c;
	}
	public Skill lastact(Character user){
		if(user==p1){
			return p1act;
		}
		else if(user==p2){
			return p2act;
		}
		else{
			return null;
		}
	}

	public void offerImage(String path, String artist){
		imagePath = path;
	}

	public void forfeit(Character player){
		end();
	}

	public Position getStance() {
		return stance;
	}
	
	public void checkStanceStatus(Character c, Position oldStance, Position newStance) {
		if ((oldStance.prone(c) || !oldStance.mobile(c)) && (!newStance.prone(c) && newStance.mobile(c))) {
			c.add(this, new Braced(c));
			c.add(this, new Wary(c, 3));
		} else if (!oldStance.mobile(c) && newStance.mobile(c)) {
			c.add(this, new Wary(c, 3));
		}
	}

	public void setStance(Position newStance) {
		if (Global.isDebugOn(DebugFlags.DEBUG_SCENE)) {
			System.out.printf("Stance Change: %s -> %s\n", stance.getClass().getName(), newStance.getClass().getName());
		}
		checkStanceStatus(p1, stance, newStance);
		checkStanceStatus(p2, stance, newStance);
		
		if (stance.inserted() && !newStance.inserted()) {
			List<BodyPart> parts1 = stance.partsFor(p1);
			List<BodyPart> parts2 = stance.partsFor(p2);
			parts1.forEach(part -> parts2.forEach(other -> part.onEndPenetration(this, p1, p2, other)));
			parts2.forEach(part -> parts1.forEach(other -> part.onEndPenetration(this, p2, p1, other)));
		} else if (!stance.inserted() && newStance.inserted()) {
			List<BodyPart> parts1 = newStance.partsFor(p1);
			List<BodyPart> parts2 = newStance.partsFor(p2);
			parts1.forEach(part -> parts2.forEach(other -> part.onStartPenetration(this, p1, p2, other)));
			parts2.forEach(part -> parts1.forEach(other -> part.onStartPenetration(this, p2, p1, other)));
		}
		
		this.stance = newStance;
		offerImage(stance.image(), "");
	}
	public Character getOther(Character affected) {
		return affected == p1 ? p2: p1;
	}
	public void writeSystemMessage(String battleString) {
		if (Global.checkFlag(Flag.systemMessages)) {
			write(battleString);
		}
	}
	public void writeSystemMessage(Character character, String string) {
		if (Global.checkFlag(Flag.systemMessages)) {
			write(character, string);
		}
	}
}
