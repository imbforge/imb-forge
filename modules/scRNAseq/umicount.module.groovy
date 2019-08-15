// Notes:
//  * Indentation is important in this file. Please, use 4 spaces for indent. *NO TABS*.

load PIPELINE_ROOT + "/modules/scRNAseq/umicount.vars.groovy"

umicount = {
    doc title: "Deduplication and Counting reads per gene",
        desc: "Deduplication and counting of mapped data and splitting accoring to cellbarcode with umi_tools",
        constraints: "",
        bpipe_version: "tested with bpipe 0.9.9.3",
        author: "Nastasja Kreim"

      // create the log folder if it doesn't exists
      def UMICOUNT_LOGDIR = new File( LOGS + "/umicount")
      if (!UMICOUNT_LOGDIR.exists()) {
          UMICOUNT_LOGDIR.mkdirs()
      }

    def UMICOUNT_FLAGS =    UMICOUNT_LOG + " " +
                            UMICOUNT_PARAM + " " +
                            UMICOUNT_EXTRA
    output.dir = UMICOUNT_OUTDIR
    if(ESSENTIAL_PAIRED == "yes"){
      UMICOUNT_FLAGS = UMICOUNT_FLAGS + " --paired"
    }

    def TOOL_ENV = prepare_tool_env("umitools", tools["umitools"]["version"], tools["umitools"]["runenv"])

    // run the chunk
    transform(".bam\$") to (".umicount.tsv.gz") {
        def SAMPLENAME = input.prefix
        exec """
            ${TOOL_ENV} &&

            SAMPLENAME_BASE=\$(basename ${SAMPLENAME}) &&

            umi_tools count $UMICOUNT_FLAGS -I $input -S $output1 -L ${UMICOUNT_LOGDIR}/\${SAMPLENAME_BASE}.umicount.log -E ${UMICOUNT_LOGDIR}/\${SAMPLENAME_BASE}.umicount.error 
        ""","umicount"
    }
}
