package me.loki2302

import groovy.sql.Sql
import me.loki2302.reports.RepositoriesWithFirstAndLastCommitDatesReport
import me.loki2302.reports.TopRepositoriesByCommitCountReport

class AnalyticsApp {
    static void main(String[] args) {
        runAnalytics()
    }

    static void runAnalytics() {
        def sql = Sql.newInstance('jdbc:hsqldb:mem:test', 'sa', '', 'org.hsqldb.jdbc.JDBCDriver')
        def databaseFacade = new DatabaseFacade(sql)
        databaseFacade.init()

        def dataLoader = new DataLoader()
        dataLoader.loadData(databaseFacade)

        def topRepositoriesByCommitCountReport = new TopRepositoriesByCommitCountReport()
        def rows = topRepositoriesByCommitCountReport.make(sql, 3)
        println(rows.collect {
            "${it.name} - ${it.commits}"
        }.join('\n'))

        // TODO: list all repositories with firstCommitDate and lastCommitDate, order by name
        def repositoriesWithFirstAndLastCommitDatesReport = new RepositoriesWithFirstAndLastCommitDatesReport()
        rows = repositoriesWithFirstAndLastCommitDatesReport.make(sql)
        println(rows.take(3).collect {
            "${it.name} - ${it.firstCommitDate} - ${it.lastCommitDate}"
        }.join('\n'))

        // TODO: list top 3 repositories with earliest firstCommitDate
        // TODO: list top 3 repositories with latest lastCommitDate
        // TODO: list top 3 repositories which have largest difference between firstCommitDate and lastCommitDate, specify first+last+difference
        // TODO: get last 3 commits for each repository (master -> details)

        /*println sql.rows('''
select top 3
    R.name as name,
    (select min(C.date) from Commits as C where C.repositoryId = R.id) as firstCommitDate
from Repositories as R
order by firstCommitDate asc''').join('\n')*/
    }
}
