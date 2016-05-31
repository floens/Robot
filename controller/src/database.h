#ifndef DATABASE_H_
#define DATABASE_H_

struct fine_result {
    int fine;
    char *owner;
};

struct fine_result *database_get_fine(const char *numberplate);

#endif
