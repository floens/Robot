#include "database.h"

#include <string.h>

typedef unsigned int uint;
typedef unsigned long ulong;

#include <my_global.h>
#include <mysql.h>
#include <stdlib.h>

#define DB_SERVER "serv"
#define DB_USER "user"
#define DB_PASS "pass"
#define DB_DB "db"

struct fine_result *database_get_fine(const char *numberplate) {
    MYSQL *con = mysql_init(NULL);
    MYSQL_STMT *statement = NULL;
    struct fine_result *fine_result = NULL;

    int has_result = 0;
    int fine = -1;
    char owner[255];

    if (con == NULL) {
        fprintf(stderr, "mysql init problem %s\n", mysql_error(con));
        goto done;
    }

    printf("Connecting to db\n");
    if (mysql_real_connect(con, DB_SERVER, DB_USER, DB_PASS, DB_DB, 0, NULL, 0) == NULL) {
        fprintf(stderr, "%s\n", mysql_error(con));
        goto done;
    }

    printf("Connected\n");

    statement = mysql_stmt_init(con);
    const char *statement_query = "SELECT fine, name FROM row WHERE license_plate = ?";
    if (mysql_stmt_prepare(statement, statement_query, strlen(statement_query))) {
        printf("mysql_stmt_prepare failed\n");
        goto done;

    }

    MYSQL_BIND param[1];

    memset(param, 0, sizeof(param));

    param[0].buffer_type = MYSQL_TYPE_STRING;
    param[0].buffer = numberplate;
    param[0].buffer_length = strlen(numberplate);
    param[0].is_null = 0;
    param[0].length = 0;

    if (mysql_stmt_bind_param(statement, param)) {
        printf("mysql_stmt_bind_param failed %s\n", mysql_stmt_error(statement));
        goto done;
    }

    MYSQL_BIND result[2];
    memset(result, 0, sizeof(result));
    result[0].buffer_type = MYSQL_TYPE_LONG;
    result[0].buffer = (void *) &fine;

    result[0].is_unsigned = 0;
    result[0].is_null = 0;
    result[0].length = 0;

    result[1].buffer_type = MYSQL_TYPE_VAR_STRING;
    result[1].buffer = owner;
    result[1].buffer_length = 255;
    result[1].is_null = 0;
    result[1].length = 0;

    if (mysql_stmt_bind_result(statement, result)) {
        printf("mysql_stmt_bind_result failed %s\n", mysql_stmt_error(statement));
        goto done;
    }

    if (mysql_stmt_execute(statement)) {
        printf("mysql_stmt_execute failed\n");
        goto done;
    }

    if (mysql_stmt_store_result(statement)) {
        printf("mysql_stmt_store_result failed\n");
        goto done;
    }

    if (mysql_stmt_fetch(statement) == 0) {
        printf("Found result %d %s\n", fine, owner);
        has_result = 1;
        fine_result = malloc(sizeof(struct fine_result));
        fine_result->fine = fine;
        fine_result->owner = owner;
    } else {
        printf("No fines found\n");
    }

    if (mysql_stmt_free_result(statement)) {
        printf("mysql_stmt_free_result failed\n");
        goto done;
    }

    done:
    if (statement != NULL) {
        mysql_stmt_close(statement);
    }

    if (con != NULL) {
        mysql_close(con);
    }

    printf("Connection closed\n");

    if (has_result) {
        return fine_result;
    } else {
        return NULL;
    }
}
