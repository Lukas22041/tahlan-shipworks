package data.scripts.hullmods;

import java.awt.Color;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShieldAPI;
import com.fs.starfarer.api.combat.ShieldAPI.ShieldType;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript;
import org.lazywizard.lazylib.MathUtils;
import org.lwjgl.util.vector.Vector2f;

public class tahlan_TemporalTravelDrive extends BaseHullMod {

    public static final float TIME_MULT_PLAYER = 20.0f;
    public static final float TIME_MULT_AI = 20.0f;

    public static final Color JITTER_COLOR = new Color(255, 106, 32,55);
    public static final Color JITTER_UNDER_COLOR = new Color(255, 54, 0,155);

    public static final float ELECTRIC_SIZE = 250.0f;

    public boolean hasFiredLightning = false;

    //Activates a pseudo-hacked periodic breaker while the ship is using its travel drive
    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {
        if (ship.getTravelDrive().isActive() && !ship.getSystem().isActive() && !ship.isHulk()) {
            //Sets the effectLevel and state variables
            float effectLevel = ship.getTravelDrive().getEffectLevel();
            ShipSystemAPI.SystemState state = ship.getTravelDrive().getState();

            //Jitter-based code
            float jitterLevel = effectLevel;
            float jitterRangeBonus = 0;
            float maxRangeBonus = 10f;
            if (state == ShipSystemAPI.SystemState.IN) {
                jitterLevel = effectLevel / (1f / ship.getTravelDrive().getChargeUpDur());
                if (jitterLevel > 1) {
                    jitterLevel = 1f;
                }
                jitterRangeBonus = jitterLevel * maxRangeBonus;
            } else if (state == ShipSystemAPI.SystemState.ACTIVE) {
                jitterLevel = 1f;
                jitterRangeBonus = maxRangeBonus;
            } else if (state == ShipSystemAPI.SystemState.OUT) {
                jitterRangeBonus = jitterLevel * maxRangeBonus;
            }
            jitterLevel = (float) Math.sqrt(jitterLevel);
            ship.setJitter(this, JITTER_COLOR, jitterLevel, 3, 0, 0 + jitterRangeBonus);
            ship.setJitterUnder(this, JITTER_UNDER_COLOR, jitterLevel, 25, 0f, 7f + jitterRangeBonus);

            //Makes the effectLevel cubed
            effectLevel *= effectLevel;
            effectLevel *= effectLevel;

            //Adjusts time mult
            float shipTimeMult = 1f;
            if (ship == Global.getCombatEngine().getPlayerShip()) {
                shipTimeMult = (float)Math.pow(TIME_MULT_PLAYER,effectLevel);
                ship.getMutableStats().getTimeMult().modifyMult("tahlan_TemporalTravelDriveDebugID", shipTimeMult);
                Global.getCombatEngine().getTimeMult().modifyMult("tahlan_TemporalTravelDriveDebugID", 1f / shipTimeMult);
            } else {
                shipTimeMult = (float)Math.pow(TIME_MULT_AI,effectLevel);
                ship.getMutableStats().getTimeMult().modifyMult("tahlan_TemporalTravelDriveDebugID", shipTimeMult);
                Global.getCombatEngine().getTimeMult().unmodify("tahlan_TemporalTravelDriveDebugID");
            }

            //Fires lightning once upon activation
            if (effectLevel >= 0.8f && !hasFiredLightning) {
                hasFiredLightning = true;
                /*Lightning based code...*/
                float tempCounter = 0;
                while (tempCounter <= (6.0f / 80f) * ELECTRIC_SIZE) {
                    Global.getCombatEngine().spawnEmpArc(ship,new Vector2f(ship.getLocation().x + MathUtils.getRandomNumberInRange(-ELECTRIC_SIZE, ELECTRIC_SIZE), ship.getLocation().y + MathUtils.getRandomNumberInRange(-ELECTRIC_SIZE, ELECTRIC_SIZE)), null, ship,
                            DamageType.ENERGY, //Damage type
                            0f, //Damage
                            0f, //Emp
                            100000f, //Max range
                            "tachyon_lance_emp_impact",
                            (1f / 8f) * ELECTRIC_SIZE, // thickness
                            JITTER_COLOR, //Central color
                            JITTER_UNDER_COLOR //Fringe Color
                    );
                    tempCounter++;
                }
                //visual effect
                Global.getCombatEngine().spawnExplosion(
                        //where
                        ship.getLocation(),
                        //speed
                        (Vector2f) new Vector2f(0,0),
                        //color
                        JITTER_COLOR,
                        //size
                        (MathUtils.getRandomNumberInRange(75f,100f) / 80f) * ELECTRIC_SIZE,
                        //duration
                        1.0f
                );
            }
        } else { //If we aren't using the travel drive, reset the values of everything
            ship.getMutableStats().getTimeMult().unmodify("tahlan_TemporalTravelDriveDebugID");
            Global.getCombatEngine().getTimeMult().unmodify("tahlan_TemporalTravelDriveDebugID");

            hasFiredLightning = false;
        }
    }

    //Prevents the hullmod from being put on ships
    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        boolean canBeApplied = false;
        return canBeApplied;
    }
}
