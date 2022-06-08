pattern_filtering = {
    doc title: "pattern filtering",
    desc:  "Filter out reads that do not contain the expected pattern for the sample",
    author: "Sergi Sayols"

    output.dir = pattern_filtering_vars.outdir

    def File f = new File(input1)
    def OUTFILE = (pattern_filtering_vars.paired ? [(f.getName() =~ /.R1.fastq.gz/).replaceFirst(".R1.filt.fastq.gz"),
                                                    (f.getName() =~ /.R2.fastq.gz/).replaceFirst(".R2.filt.fastq.gz")]
                                                 :  (f.getName() =~ /.fastq.gz/).replaceFirst(".filt.fastq.gz"))

    def pattern_filtering_INPUT = (pattern_filtering_vars.paired ? "-1 $input1 -2 $input2" : "-1 $input")
    def pattern_filtering_FLAGS = "-d -o filt"

    def PREAMBLE = get_preamble(stage:stageName, outdir:output.dir, input:new File(input1.prefix).getName())

    produce(OUTFILE) {
      exec """
        ${PREAMBLE} &&

        if [ ! -e ${pattern_filtering_vars.targets} ]; then
          echo "Targets file ${pattern_filtering_vars.targets} doesn't exist" >> $output1 &&
          exit 1;
        fi;

        FASTQ=\$(basename $input1);
        FASTQ="\${FASTQ%%.*}";
        TARGET=\$(grep "^\$FASTQ" ${pattern_filtering_vars.targets} | head -n1);
        pattern=\$(echo $TARGET | tr '\t' ' ' | cut -f2 -d" ");
        umi=\$(echo $TARGET | tr '\t' ' ' | cut -f3 -d" ");

        perl ${PIPELINE_ROOT}/tools/bliss/pattern_filtering.pl $pattern_filtering_FLAGS $pattern_filtering_INPUT -u "\$umi" -r "\$pattern";
        if [ \$? -eq 0 ]; then
          f=$input1;
          mv \${f%.fastq.gz}.filt.fastq.gz $output1;
          [ "$pattern_filtering_vars.paired" = "false" ] || (f=$input2; mv \${f%.fastq.gz}.filt.fastq.gz $output2);
        fi
      ""","pattern_filtering"
    }
}
