# RSE Project: Static Program Analyzer

## Introduction
This project implements a **static program analyzer** developed as part of the ETH Zurich Reliable Software Engineering (RSE) course. The analyzer verifies safety properties of a Java class used for delivery management.

## Motivation
The `Store` class models a delivery system where products are delivered to stores. The analyzer ensures:
1. Deliveries are non-negative.
2. Deliveries do not exceed trolley size.
3. Total deliveries do not exceed reserve capacity.

## Project Description
### Properties to Verify
The analyzer verifies the following:
- **NON_NEGATIVE**: Delivery volumes are non-negative.
- **FITS_IN_TROLLEY**: Delivery volumes do not exceed the trolley capacity.
- **FITS_IN_RESERVE**: Total deliveries do not exceed reserve capacity.

### Libraries Used
- **[APRON](http://apron.cri.ensmp.fr/library/)**: Numerical abstract domains.
- **[Soot](https://github.com/soot-oss/soot)**: Java program analysis framework.