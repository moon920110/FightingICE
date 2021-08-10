package setting;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class MctsSetting {
    public static final int TRAJECTORY_CAPACITY = 300;

    public static final String MIN_MAX_PATH = "D:\\DDA\\dataset\\min_max_df.csv";

    public static final String ARGUMENT_NAMES[] = new String[]{
            "self_time_spent_in_bin0",  // / total_frame_cnt
            "self_time_spent_in_bin1",
            "self_time_spent_in_bin2",
            "self_time_spent_in_bin3",
            "self_time_spent_in_bin4",
            "oppo_time_spent_in_bin0",
            "oppo_time_spent_in_bin1",
            "oppo_time_spent_in_bin2",
            "oppo_time_spent_in_bin3",
            "oppo_time_spent_in_bin4",
            "close_distance_ratio",  // / total_frame_cnt
            "avg_distance",  // / total_frame_cnt
            "self_approaching_ratio",  // / (self app cnt + self ma cnt)
            "self_moving_away_ratio",  // / (self app cnt + self ma cnt)
            "oppo_approaching_ratio",  // / (oppo app cnt + oppo ma cnt)
            "oppo_moving_away_ratio",  // / (oppo app cnt + oppo ma cnt)
            "self_avg_approaching_speed",  // / self app cnt
            "self_avg_moving_away_speed",  // / self ma cnt
            "oppo_avg_approaching_speed",  // / oppo app cnt
            "oppo_avg_moving_away_speed",  // / oppo ma cnt
            "self_action0_ratio", // / total_frame_cnt
            "oppo_action0_ratio",
            "self_action1_ratio",
            "oppo_action1_ratio",
            "self_action2_ratio",
            "oppo_action2_ratio",
            "self_action3_ratio",
            "oppo_action3_ratio",
            "self_action4_ratio",
            "oppo_action4_ratio",
            "self_action5_ratio",
            "oppo_action5_ratio",
            "self_action6_ratio",
            "oppo_action6_ratio",
            "self_action7_ratio",
            "oppo_action7_ratio",
            "self_action8_ratio",
            "oppo_action8_ratio",
            "self_action9_ratio",
            "oppo_action9_ratio",
            "self_action10_ratio",
            "oppo_action10_ratio",
            "self_action11_ratio",
            "oppo_action11_ratio",
            "self_action12_ratio",
            "oppo_action12_ratio",
            "self_action13_ratio",
            "oppo_action13_ratio",
            "self_action14_ratio",
            "oppo_action14_ratio",
            "self_action15_ratio",
            "oppo_action15_ratio",
            "self_action16_ratio",
            "oppo_action16_ratio",
            "self_action17_ratio",
            "oppo_action17_ratio",
            "self_action18_ratio",
            "oppo_action18_ratio",
            "self_action19_ratio",
            "oppo_action19_ratio",
            "self_action20_ratio",
            "oppo_action20_ratio",
            "self_action21_ratio",
            "oppo_action21_ratio",
            "self_action22_ratio",
            "oppo_action22_ratio",
            "self_action23_ratio",
            "oppo_action23_ratio",
            "self_action24_ratio",
            "oppo_action24_ratio",
            "self_action25_ratio",
            "oppo_action25_ratio",
            "self_action26_ratio",
            "oppo_action26_ratio",
            "self_action27_ratio",
            "oppo_action27_ratio",
            "self_action28_ratio",
            "oppo_action28_ratio",
            "self_action29_ratio",
            "oppo_action29_ratio",
            "self_action30_ratio",
            "oppo_action30_ratio",
            "self_action31_ratio",
            "oppo_action31_ratio",
            "self_action32_ratio",
            "oppo_action32_ratio",
            "self_action33_ratio",
            "oppo_action33_ratio",
            "self_action34_ratio",
            "oppo_action34_ratio",
            "self_action35_ratio",
            "oppo_action35_ratio",
            "self_action36_ratio",
            "oppo_action36_ratio",
            "self_action37_ratio",
            "oppo_action37_ratio",
            "self_action38_ratio",
            "oppo_action38_ratio",
            "self_action39_ratio",
            "oppo_action39_ratio",
            "self_action40_ratio",
            "oppo_action40_ratio",
            "self_action41_ratio",
            "oppo_action41_ratio",
            "self_action42_ratio",
            "oppo_action42_ratio",
            "self_action43_ratio",
            "oppo_action43_ratio",
            "self_action44_ratio",
            "oppo_action44_ratio",
            "self_action45_ratio",
            "oppo_action45_ratio",
            "self_action46_ratio",
            "oppo_action46_ratio",
            "self_action47_ratio",
            "oppo_action47_ratio",
            "self_action48_ratio",
            "oppo_action48_ratio",
            "self_action49_ratio",
            "oppo_action49_ratio",
            "self_action50_ratio",
            "oppo_action50_ratio",
            "self_action51_ratio",
            "oppo_action51_ratio",
            "self_action52_ratio",
            "oppo_action52_ratio",
            "self_action53_ratio",
            "oppo_action53_ratio",
            "self_action54_ratio",
            "oppo_action54_ratio",
            "self_state0_ratio",  // / total_frame_cnt
            "oppo_state0_ratio",
            "self_state1_ratio",
            "oppo_state1_ratio",
            "self_state2_ratio",
            "oppo_state2_ratio",
            "self_state3_ratio",
            "oppo_state3_ratio",
            "self_attack_type1_ratio",  // / att cnt (type1 + 2 + 3 + 4)
            "oppo_attack_type1_ratio",
            "self_attack_type2_ratio",
            "oppo_attack_type2_ratio",
            "self_attack_type3_ratio",
            "oppo_attack_type3_ratio",
            "self_attack_type4_ratio",
            "oppo_attack_type4_ratio",
            "self_attack_ratio",  // / total_frame_cnt
            "oppo_attack_ratio",
            "self_attack_avg_damage",  // / att cnt
            "oppo_attack_avg_damage",
            "self_projectiles_type1_ratio",  // / proj cnt (type1 + 2 + 3 + 4)
            "oppo_projectiles_type1_ratio",
            "self_projectiles_type2_ratio",
            "oppo_projectiles_type2_ratio",
            "self_projectiles_type3_ratio",
            "oppo_projectiles_type3_ratio",
            "self_projectiles_type4_ratio",
            "oppo_projectiles_type4_ratio",
            "self_projectiles_ratio",  // /total_frame_cnt
            "oppo_projectiles_ratio",
            "self_projectiles_avg_damage",  // / proj cnt
            "oppo_projectiles_avg_damage",
            "self_avg_projectiles_num",  // / proj cnt
            "oppo_avg_projectiles_num",
            "self_be_hit_per_second",  // / total_frame_cnt * 60
            "self_hit_per_second",   // / total_frame_cnt * 60
            "self_guard_per_second",  // / total_frame_cnt * 60
            "self_blocked_per_second",  // / total_frame_cnt * 60
            "avg_hp_diff",  // / total_frame_cnt
            "self_hp_sup_ratio",  // / total_frame_cnt
            "oppo_hp_sup_ratio",
            "avg_hp_zero_crossing",  // / total_frame_cnt * 60
            "avg_self_hp_reducing_speed",  // / total_frame_cnt * 60
            "avg_oppo_hp_reducing_speed",
            "avg_self_energy_gaining_speed",  // / total_frame_cnt * 60
            "avg_oppo_energy_gaining_speed",
            "avg_self_energy_reducing_speed",  // / total_frame_cnt * 60
            "avg_oppo_energy_reducing_speed",
    };
}
