package kz.bsbnb.controller;

import kz.bsbnb.common.bean.*;
import kz.bsbnb.common.consts.Role;
import kz.bsbnb.common.model.*;
import kz.bsbnb.util.SimpleResponse;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by kanattulbassiyev on 8/7/16.
 */
public interface IUserController {
    List<UserBean> getUsers(int page, int count);

    User getUserById(Long id);

    SimpleResponse getUserByIdSimple(Long id);

    SimpleResponse checkUser(User user);

    SimpleResponse updateProfileUser(RegUserBean userBean);

    SimpleResponse deleteUser(User user);

    UserBean castUser(User user);

    SimpleResponse complateVoting(Long votingId, Long userId, ConfirmBean bean);

    List<QuestionBean> getVotingQuestions(Long votingId, Long userId);

    QuestionBean castFromQuestion(Question q, User user, boolean showPdf);

    boolean canVote(Voting voting, User user);

    DecisionBean getBeanFromDecision(Decision decision);

    VotingBean castToBean(Voting voting, User user);

    SimpleResponse getUserpProfile(Long userId);

    SimpleResponse verifyIIN(String iin);

    Role getRole(User user, Organisation organisation);

    Role getRole(User user);

    String getFullName(UserInfo userInfo);

    List<SimpleDecisionBean> getDecisionsForQuestion(Long questionId);
}
