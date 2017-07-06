def getLabelNum(version) {
    def vs = version.split("\\.")
    return "" + vs[0] + vs[1]
}

def getLabelStr(geo_v, ns_v, ne_v) {
    return "not geo" + geo_v + "_global" + ns_v + "_instance" + ne_v
}

def getLabel(label_list, geo_v, ns_v, ne_v) {
    def label_collection = []
    for (l in label_list) {
        def ls = l.split("-")
        def gv = ls[0]
        def nsv = ls[1]
        def nev = ls[2]
        if (gv == getLabelNum(geo_v) && nsv == getLabelNum(ns_v) && nev == getLabelNum(ne_v)) {
            continue
        } else {
            label_collection = label_collection + getLabelStr(gv, nsv, nev)
        }
    }
    def labels = ""
    for (int i = 0; i < label_collection.size(); i++) {
        if (i != 0) {
            labels = labels + " and "
        }
        labels = labels + label_collection[i]
    }
    return labels
}

node {
    def env = "qa2"
    def prod_build
    def prod_branch
    def dev_build
    def dev_branch
    def geo_prod_build
    def geo_dev_build

    def label_list = []


    stage('Preparation') {
        git credentialsId: 'git-001', url: 'https://github.com/ansonLx/jenkins-jobs.git'

        prod_build = sh(script: 'cat ./sh/ne-version | grep "^prod_build=" | sed "s/^prod_build=//"', returnStdout: true).trim()
        prod_branch = sh(script: 'cat ./sh/ne-version | grep "^prod_branch=" | sed "s/^prod_branch=//"', returnStdout: true).trim()
        dev_build = sh(script: 'cat ./sh/ne-version | grep "^dev_build=" | sed "s/^dev_build=//"', returnStdout: true).trim()
        dev_branch = sh(script: 'cat ./sh/ne-version | grep "^dev_branch=" | sed "s/^dev_branch=//"', returnStdout: true).trim()
        geo_prod_build = sh(script: 'cat ./sh/ne-version | grep "^geo_prod_build=" | sed "s/^geo_prod_build=//"', returnStdout: true).trim()
        geo_dev_build = sh(script: 'cat ./sh/ne-version | grep "^geo_dev_build=" | sed "s/^geo_dev_build=//"', returnStdout: true).trim()

        echo "${prod_build}, ${dev_build}, ${geo_prod_build}, ${geo_dev_build}"

        label_list = label_list + (getLabelNum(geo_prod_build) + "-" + getLabelNum(prod_build) + "-" + getLabelNum(dev_build))
//        label_list = label_list + (getLabelNum(geo_prod_build) + "-" + getLabelNum(prod_build) + "-" + getLabelNum(prod_build))
        label_list = label_list + (getLabelNum(geo_prod_build) + "-" + getLabelNum(dev_build) + "-" + getLabelNum(dev_build))
        label_list = label_list + (getLabelNum(geo_prod_build) + "-" + getLabelNum(dev_build) + "-" + getLabelNum(prod_build))
        label_list = label_list + (getLabelNum(geo_dev_build) + "-" + getLabelNum(prod_build) + "-" + getLabelNum(dev_build))
        label_list = label_list + (getLabelNum(geo_dev_build) + "-" + getLabelNum(prod_build) + "-" + getLabelNum(prod_build))
        label_list = label_list + (getLabelNum(geo_dev_build) + "-" + getLabelNum(dev_build) + "-" + getLabelNum(dev_build))
        label_list = label_list + (getLabelNum(geo_dev_build) + "-" + getLabelNum(dev_build) + "-" + getLabelNum(prod_build))

    }

    stage("deploy geo_version->${geo_prod_build} ns_version->${dev_build} ne_version->${prod_build}") {
        echo "deploy ${env} geo-version: ${geo_prod_build} ns-version: ${dev_build} ne-version: ${prod_build}"
        sh(script: "./sh/deploy_test.sh ${prod_build} ${dev_build}")
    }
    stage("test geo_version->${geo_prod_build} ns_version->${dev_build} ne_version->${prod_build}") {
        def test_label = getLabel(label_list, geo_prod_build, dev_build, prod_build)
        echo "run test job geo-version: ${geo_prod_build} ns-version: ${dev_build} ne-version: ${prod_build} label: ${test_label}"

        build job: 'run test job 01', parameters: [
                string(name: 'geo_version', value: "${geo_prod_build}"),
                string(name: 'ne_version', value: "${dev_build}"),
                string(name: 'ns_version', value: "${prod_build}"),
                string(name: 'test_label', value: "${test_label}")]
    }

    stage("deploy geo_version->${geo_prod_build} ns_version->${dev_build} ne_version->${dev_build}") {
        echo "deploy ${env} geo-version: ${geo_prod_build} ns-version: ${dev_build} ne-version: ${dev_build}"
        sh(script: "./sh/deploy_test.sh ${prod_build} ${dev_build}")
    }
    stage("test geo_version->${geo_prod_build} ns_version->${dev_build} ne_version->${dev_build}") {
        def test_label = getLabel(label_list, geo_prod_build, dev_build, dev_build)
        echo "run test job geo-version: ${geo_prod_build} ns-version: ${dev_build} ne-version: ${dev_build} label: ${test_label}"

        build job: 'run test job 01', parameters: [
                string(name: 'geo_version', value: "${geo_prod_build}"),
                string(name: 'ne_version', value: "${dev_build}"),
                string(name: 'ns_version', value: "${dev_build}"),
                string(name: 'test_label', value: "${test_label}")]
    }
}
