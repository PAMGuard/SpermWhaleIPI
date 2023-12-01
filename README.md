# SpermWhaleIPI

The "Sperm whale IPI Plugin" (AKA IPI plugin) is an external plugin for measuring the Inter-Pulse Interval (IPI) of the clicks of sperm whales. Measurement of IPI has been demonstrated to be a reliable means of estimating the total length of an individual sperm whale (Gordon 1991; Growcott et al 2011). The module employs two related signal processing methods, both based on cepstral analysis of clicks, to estimate IPI:

Method 1 computes the IPI for each click and then creates a histogram from all of these measurements. Mean and mode of these individual IPI estimates are then computed to provide an IPI estimate similar to that described by Gordon (1991 - J. Zool. Lond. Vol 224 pp 301-314).

Method 2 computes the ensemble averaged cepstrum from all clicks. The IPI is estimated from the peak value of this ensemble averaged signal. This method provides estimates of IPI in the manner described by Teloni et al (2007 - J. Cetacean Res. Manage. Vol 9(2), pp 127-136).

The IPI plugin was first published in 2009 (Pamguard version 1.6), and was updated in May 2018 to work as an external plugin with modern versions of PAMGuard (>=2.0.12c), and has been used in several peer reviewed publications (Growcott et al 2011, Miller et al 2013). The IPI plugin can be run in either Normal and Mixed Mode.

If you use this module to analyse data and wish to cite the IPI plugin please use the following reference:

Miller, B. S., Growcott, A., Slooten, E., and Dawson, S. M. (2013). Acoustically derived growth rates of sperm whales (Physeter macrocephalus) in Kaikoura, New Zealand. J. Acoust. Soc. Am., 134, 2438–45. doi:10.1121/1.4816564.

 
