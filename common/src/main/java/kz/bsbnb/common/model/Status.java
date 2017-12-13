package kz.bsbnb.common.model;

/**
 * Created by Olzhas.Pazyldayev on 15.09.2016.
 */
public enum Status {
    NEW, // новый
    ACTIVE, //активная
    BLOCKED, //заблокированная
    ARCHIVED, //архивны
    PENDING, //в ожидании
    PENDING_CANCELED, //в ожидании отменено по истечении таймаута
    APPROVING, // в ожидании авторизации в енпф или цдцб, или другое
    APPROVED, //Одобрена
    DELETED, //Удалена
    DENIED,
    ERROR, // Ошибка
    CLOSED, //закрыта
    CLOSED_CANCELED, //закрыт по отмене
    CLOSED_FINAL,
    SUCCESS,//успешна
    ISSUED,
    NOT_FOUND,
    FAIL, //неудачная попытка
    READ,
    DEPLOYING,
    ERROR_HANG,
    AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FAILURE,
    AUTHORIZATION_FAILURE;


    public static Status getStatus(String status) {
        return valueOf(status);
    }

}
