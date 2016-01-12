package nightgames.stance;


import java.util.ArrayList;
import java.util.List;

import nightgames.characters.Character;
import nightgames.global.Global;

public abstract class AbstractBehindStance extends Position {
	public AbstractBehindStance(Character top, Character bottom, Stance stance) {
		super(top, bottom, stance);
	}

	@Override
	public Position insertRandomDom(Character dom) {
		List<Position> possibleResults = new ArrayList<>();
		Character sub = getOther(dom);
		if (dom.hasInsertable() && sub.hasPussy()) {
			Position newPos = insert(dom, dom);
			if (newPos != this)
				possibleResults.add(newPos);
		}
		if (dom.hasPussy() && sub.hasInsertable()){
			Position newPos = insert(sub, dom);
			if (newPos != this)
				possibleResults.add(newPos);
		}
		if (possibleResults.isEmpty()) {
			return this;
		} else {
			return possibleResults.get(Global.random(possibleResults.size()));
		}
	}

	@Override
	public Position insert(Character pitcher, Character dom) {
		Character catcher = getOther(pitcher);
		Character sub = getOther(dom);
		if (pitcher.body.getRandomInsertable() == null || !catcher.hasPussy()) {
			// invalid
			return this;
		}
		if (pitcher == dom && pitcher == top) {
			// guy is holding girl from behind, and is the dominant one in the new stance
			return new Doggy(pitcher, catcher);
		}
		if (pitcher == sub && pitcher == top) {
			// guy is holding girl from behind, and is the submissive one in the new stance
			return new ReverseCowgirl(catcher, pitcher);
		}
		if (pitcher == dom && pitcher == bottom) {
			// girl is holding guy from behind, and is the submissive one in the new stance
			return new UpsideDownMaledom(pitcher, catcher);			
		}
		if (pitcher == sub && pitcher == bottom) {
			// girl is holding guy from behind, and is the dominant one in the new stance
			return new ReverseCowgirl(catcher, pitcher);
		}
		return this;
	}
}
