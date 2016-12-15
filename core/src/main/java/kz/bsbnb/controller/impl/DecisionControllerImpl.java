package kz.bsbnb.controller.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.bsbnb.common.bean.DecisionBean;
import kz.bsbnb.common.bean.TotalDecision;
import kz.bsbnb.common.model.*;
import kz.bsbnb.controller.IDecisionController;
import kz.bsbnb.controller.IVotingController;
import kz.bsbnb.repository.IDecisionRepository;
import kz.bsbnb.repository.IQuestionRepository;
import kz.bsbnb.repository.IUserRepository;
import kz.bsbnb.util.CryptUtil;
import kz.bsbnb.util.JsonUtil;
import kz.bsbnb.util.SimpleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ruslan on 21.10.2016
 */
@RestController
@RequestMapping(value = "/decision")
public class DecisionControllerImpl implements IDecisionController {

    @Autowired
    IDecisionRepository decisionRepository;
    @Autowired
    IQuestionRepository questionRepository;
    @Autowired
    IUserRepository userRepository;
    @Autowired
    IVotingController votingController;


    @Override
    @RequestMapping(value = "/new", method = RequestMethod.POST)
    public SimpleResponse regDecision(@RequestBody @Valid DecisionBean bean) {
        SimpleResponse result = new SimpleResponse();
        Decision dec = votingController.getDecisionFromBean(bean);
        List<Decision> oldDecitions = decisionRepository.findByQuestionIdAndVoterId(dec.getQuestionId(), dec.getVoterId());
        if (dec.getQuestionId() == null) {
            result.setData("Вопрос не найден").ERROR_CUSTOM();
        } else if (dec.getVoterId() == null) {
            result.setData("Голосующий не найден").ERROR_CUSTOM();
        } else if (dec.getVoterId().getShareCount() < bean.getScore()) {
            result.setData("Очки больше доступного").ERROR_CUSTOM();
        } else if (bean.getScore() < 0) {
            result.setData("Очки не могут быть отрицательными").ERROR_CUSTOM();
        } else if (dec != null) {
            if (dec.getVoterId().getDateVoting() != null) {
                return new SimpleResponse("Вы уже проголосовали").ERROR_CUSTOM();
            } else if (!oldDecitions.isEmpty()) {
                return new SimpleResponse("Вы уже проголосовали за это вопрос").ERROR_CUSTOM();
            } else {
                dec.setStatus("NEW");
                dec = decisionRepository.save(dec);
                result.setData(dec).SUCCESS();
            }
        } else {
            result.setData("Не возможно сохранить ваше решение").ERROR_CUSTOM();
        }
        votingController.updateQuestionDecisions(bean.getQuestionId());
        return result;
    }

    @Override
    @RequestMapping(value = "/kill", method = RequestMethod.DELETE)
    public SimpleResponse delDecision(@RequestBody @Valid DecisionBean bean) {
        SimpleResponse result = new SimpleResponse();
        Decision dec = votingController.getDecisionFromBean(bean);
        List<Decision> oldDecitions = decisionRepository.findByQuestionIdAndVoterId(dec.getQuestionId(), dec.getVoterId());
        if (!oldDecitions.isEmpty()) {
            for (Decision decision : oldDecitions) {
                decision.setStatus("KILLED");
                decisionRepository.save(decision);
//                decisionRepository.deleteByIds(decision.getId());
            }
            votingController.updateQuestionDecisions(dec.getQuestionId().getId());
            result.setData("Решение отменено").SUCCESS();
        } else {
            result.setData("Решение не найдено").ERROR_CUSTOM();
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/newList", method = RequestMethod.POST)
    public SimpleResponse regDecision(@RequestBody @Valid List<DecisionBean> beans) {
        SimpleResponse result = new SimpleResponse();
        List<Decision> oldDecisions = new ArrayList<>();
        for (DecisionBean bean : beans) {
            Decision dec = votingController.getDecisionFromBean(bean);
            oldDecisions = decisionRepository.findByQuestionIdAndVoterId(dec.getQuestionId(), dec.getVoterId());
        }
        List<Object> decisions = new ArrayList<>();
        String str = "";
        boolean hasError = false;
        for (DecisionBean bean : beans) {
            Decision dec = votingController.getDecisionFromBean(bean);
            if (dec.getQuestionId() == null) {
                decisions.add("Вопрос не найден");
                str = "Вопрос не найден";
                hasError = true;
            } else if (dec.getVoterId() == null) {
                decisions.add("Голосующий не найден");
                str = "Голосующий не найден";
                hasError = true;
            } else if (dec.getVoterId().getShareCount() < getAllScore(dec.getVoterId(), dec.getQuestionId(), beans)) {
                decisions.add("Очки больше доступного");
                str = "Очки больше доступного";
                hasError = true;
            } else if (bean.getScore() < 0) {
                decisions.add("Очки не могут быть отрицательными");
                str = "Очки не могут быть отрицательными";
                hasError = true;
            } else if (dec != null) {
                if (dec.getVoterId().getDateVoting() != null) {
                    return new SimpleResponse("Вы уже проголосовали").ERROR_CUSTOM();
                } else if (!oldDecisions.isEmpty()) {
//                    for (Decision decision : oldDecisions) {
//                        decisionRepository.deleteByIds(decision.getId());
//                    }
                    return new SimpleResponse("Вы уже проголосовали за это вопрос").ERROR_CUSTOM();
                } else {
                    dec.setStatus("NEW");
                    dec = decisionRepository.save(dec);
                    decisions.add(dec);
                }
            } else {
                decisions.add("Не возможно сохранить ваше решение для (" + bean.toString() + ")");
                str = "Не возможно сохранить ваше решение для (" + bean.toString() + ")";
                hasError = true;
            }
        }
        if (hasError) {
            result.setData(str).ERROR_CUSTOM();
        } else {
            result.setData(decisions).SUCCESS();
        }
        if (!beans.isEmpty()) {
            votingController.updateQuestionDecisions(beans.get(0).getQuestionId());
        }
        return result;
    }

    @Override
    @RequestMapping(value = "/newWithCheck", method = RequestMethod.POST)
    public SimpleResponse regCheckDecision(@RequestBody @Valid DecisionBean bean) {
        SimpleResponse result = new SimpleResponse();
        Decision dec = votingController.getDecisionFromBean(bean);
        List<Decision> oldDecitions = decisionRepository.findByQuestionIdAndVoterId(dec.getQuestionId(), dec.getVoterId());
        if (dec.getQuestionId() == null) {
            result.setData("Вопрос не найден").ERROR_CUSTOM();
        } else if (dec.getVoterId() == null) {
            result.setData("Голосующий не найден").ERROR_CUSTOM();
        } else if (dec.getVoterId().getShareCount() < bean.getScore()) {
            result.setData("Очки больше доступного").ERROR_CUSTOM();
        } else if (bean.getScore() < 0) {
            result.setData("Очки не могут быть отрицательными").ERROR_CUSTOM();
        } else if (dec != null) {
            if (dec.getVoterId().getDateVoting() != null) {
                return new SimpleResponse("Вы уже проголосовали").ERROR_CUSTOM();
            } else if (!oldDecitions.isEmpty()) {
                return new SimpleResponse("Вы уже проголосовали за это вопрос").ERROR_CUSTOM();
            } else {
                if (dec.getQuestionId().getQuestionType().equals("ORDINARY") && dec.getAnswerId() == null && dec.getComments() == null) {
                    result.setData("Ваше решение - пустое").ERROR_CUSTOM();
                } else {
                    CheckDecision check = new CheckDecision(false);
                    Object obj = checkAndSave(bean, dec, check);
                    if (check.isHasError()) {
                        System.out.println(obj);
                        result.setData(obj).ERROR_CUSTOM();
                    } else {
                        dec = (Decision) obj;
                        decisionRepository.save(dec);
                        result.setData(dec).SUCCESS();
                    }
                }
            }
        } else {
            result.setData("Не возможно сохранить ваше решение").ERROR_CUSTOM();
        }
        votingController.updateQuestionDecisions(bean.getQuestionId());
        return result;
    }

    private Object checkAndSave(DecisionBean bean, Decision dec, CheckDecision check) {
        String strUserId = CryptUtil.getValue(bean.getConfirm().getXmlBody(), "userId");
        String strQuestionId = CryptUtil.getValue(bean.getConfirm().getXmlBody(), "questionId");
        String strAnswerId = CryptUtil.getValue(bean.getConfirm().getXmlBody(), "answerId");
        CryptUtil.VerifyIIN result = CryptUtil.verifyXml(bean.getConfirm().getXmlBody());
        boolean bUserId = false, bQuestionId = false, bAnswerId = false;
        if (result.isVerify()) {
            if (strUserId != null && !strUserId.equals("") && String.valueOf(bean.getUserId()).equals(strUserId)) {
                bUserId = true;
            }
            if (strQuestionId != null && !strQuestionId.equals("") && String.valueOf(bean.getQuestionId()).equals(strQuestionId)) {
                bQuestionId = true;
            }
            if (bean.getAnswerId() != null && !bean.getAnswerId().equals("")) {
                if (strAnswerId != null && !strAnswerId.equals("") && String.valueOf(bean.getAnswerId()).equals(strAnswerId)) {
                    bAnswerId = true;
                }
            } else {
                bAnswerId = true;
            }
            User user = userRepository.findOne(bean.getUserId());
            if (user != null && user.getIin().equals(result.getIin())) {
                if (bUserId && bQuestionId && bAnswerId) {
                    dec.setStatus("NEW");
//                    dec = decisionRepository.save(dec);
                    return dec;
                } else {
                    check.setHasError(true);
                    return "Подписанные данные не совпадают";
                }
            } else {
                check.setHasError(true);
                return "ИИН подписанта и сертификата не совпадает";
            }
        } else {
            check.setHasError(true);
            return "Не проверено";
        }
    }

    @Override
    @RequestMapping(value = "/newListWithCheck", method = RequestMethod.POST)
    public SimpleResponse regCheckDecision(@RequestBody @Valid List<DecisionBean> beans) {
        SimpleResponse result = new SimpleResponse();
        List<Decision> oldDecisions = new ArrayList<>();
        for (DecisionBean bean : beans) {
            Decision dec = votingController.getDecisionFromBean(bean);
            oldDecisions = decisionRepository.findByQuestionIdAndVoterId(dec.getQuestionId(), dec.getVoterId());
        }
        List<Object> decisions = new ArrayList<>();
        String str = "Ваше решение - пустое";
        CheckDecision check = new CheckDecision(false);
        for (DecisionBean bean : beans) {
            Decision dec = votingController.getDecisionFromBean(bean);
            if (dec.getQuestionId() == null) {
                decisions.add("Вопрос не найден");
                str = "Вопрос не найден";
                check.setHasError(true);
            } else if (dec.getVoterId() == null) {
                decisions.add("Голосующий не найден");
                str = "Голосующий не найден";
                check.setHasError(true);
            } else if (dec.getVoterId().getShareCount() < getAllScore(dec.getVoterId(), dec.getQuestionId(), beans)) {
                decisions.add("Очки больше доступного");
                str = "Очки больше доступного";
                check.setHasError(true);
            } else if (bean.getScore() < 0) {
                decisions.add("Очки не могут быть отрицательными");
                str = "Очки не могут быть отрицательными";
                check.setHasError(true);
            } else if (dec != null) {
                if (dec.getVoterId().getDateVoting() != null) {
                    return new SimpleResponse("Вы уже проголосовали").ERROR_CUSTOM();
                } else if (!oldDecisions.isEmpty()) {
//                    for (Decision decision : oldDecisions) {
//                        decisionRepository.deleteByIds(decision.getId());
//                    }
                    return new SimpleResponse("Вы уже проголосовали за это вопрос").ERROR_CUSTOM();
                } else {

                    if (dec.getAnswerId() != null) {
                        Object obj = checkAndSave(bean, dec, check);
                        if (check.isHasError()) {
                            str = (String) obj;
                        }
                        decisions.add(obj);
                    } else {
                        if (!check.isHasError()) {
                            dec.setStatus("NEW");
                            decisions.add(dec);
                        }
                    }
                }
            } else {
                decisions.add("Не возможно сохранить ваше решение для (" + bean.toString() + ")");
                str = "Не возможно сохранить ваше решение для (" + bean.toString() + ")";
                check.setHasError(true);
            }
        }
        if (check.isHasError()) {
            System.out.println("DecisionController error ="+str);
            result.setData(str).ERROR_CUSTOM();
        } else {
            for (Object obj:decisions) {
                decisionRepository.save((Decision) obj);
            }
            result.setData(decisions).SUCCESS();
        }
        if (!beans.isEmpty()&&!check.isHasError()) {
            votingController.updateQuestionDecisions(beans.get(0).getQuestionId());
        }
        return result;
    }

    private int getAllScore(Voter voterId, Question questionId, List<DecisionBean> beans) {
        int result = 0;
        for (DecisionBean bean : beans) {
            if (bean.getUserId().equals(voterId.getUserId().getId()) && bean.getQuestionId().equals(questionId.getId())) {
                result += bean.getScore();
            }
        }
        return result;
    }


    class CheckDecision {
        private String errorText;
        private boolean hasError;

        public CheckDecision(boolean hasError) {
            this.hasError = hasError;
        }

        public String getErrorText() {
            return errorText;
        }

        public void setErrorText(String errorText) {
            this.errorText = errorText;
        }

        public boolean isHasError() {
            return hasError;
        }

        public void setHasError(boolean hasError) {
            this.hasError = hasError;
        }
    }
}
