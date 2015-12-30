__author__ = 'Hwaipy'
import enum
import threading
import math
import sys


class UnitPrefix:
    def __init__(self, prefix, name, factor=1):
        self.prefix = prefix
        self.name = name
        self.factor = factor

    prefixMap = {}
    nameMap = {}
    registerLock = threading.Lock()

    @staticmethod
    def register(unitPrefix):
        if not isinstance(unitPrefix, UnitPrefix):
            raise TypeError
        UnitPrefix.registerLock.acquire()
        if UnitPrefix.prefixMap.__contains__(unitPrefix.prefix):
            raise RuntimeError('Prefix {} exists.'.format(unitPrefix.prefix))
        if UnitPrefix.nameMap.__contains__(unitPrefix.name):
            raise RuntimeError('Prefix name {} exists.'.format(unitPrefix.name));
        UnitPrefix.prefixMap[unitPrefix.prefix] = unitPrefix
        UnitPrefix.nameMap[unitPrefix.name] = unitPrefix
        UnitPrefix.registerLock.release()

    @staticmethod
    def getRegisteredPrefixes():
        return [v for v in UnitPrefix.prefixMap.values()]

    @staticmethod
    def get(prefix):
        return UnitPrefix.prefixMap.get(prefix, None)

    @staticmethod
    def create(prefix, name, factor=1):
        unitPrefix = UnitPrefix(prefix, name, factor)
        UnitPrefix.register(unitPrefix)
        return unitPrefix


class SIBaseUnit(enum.Enum):
    # The distance travelled by light in vacuum in 1/299792458 second.(1983)
    m = 0
    # The mass of the international prototype kilogram.(1889)
    kg = 1
    # The duration of 9192631770 periods of the radiation corresponding to the transition between the two hyperfine
    # levels of the ground state of the caesium 133 atom.(1967)
    s = 2
    # The constant current which, if maintained in two straight parallel conductors of infinite length, of negligible
    # circular cross-section, and placed 1 m apart in vacuum, would produce between these conductors a force equal to
    # 2×10^−7 newtons per metre of length.(1946)
    A = 3
    # 1/273.16 of the thermodynamic temperature of the triple point of water.(1967)
    K = 4
    # The amount of substance of a system which contains as many elementary entities as there are atoms in 0.012
    # kilogram of carbon 12.(1967)
    mol = 5
    # The luminous intensity, in a given direction, of a source that emits monochromatic radiation of frequency
    # 540×1012 hertz and that has a radiant intensity in that direction of 1/683 watt per steradian.(1979)
    cd = 6


class Unit:
    def __init__(self, token, hasPrefix=True, factor=1, powerM=0, powerKG=0, powerS=0, powerA=0, powerK=0, powerMOL=0,
                 powerCD=0):
        self.token = token
        self.hasPrefix = hasPrefix
        self.factor = factor
        self.powers = [0] * 7
        self.powers[SIBaseUnit.m.value] = powerM
        self.powers[SIBaseUnit.kg.value] = powerKG
        self.powers[SIBaseUnit.s.value] = powerS
        self.powers[SIBaseUnit.A.value] = powerA
        self.powers[SIBaseUnit.K.value] = powerK
        self.powers[SIBaseUnit.mol.value] = powerMOL
        self.powers[SIBaseUnit.cd.value] = powerCD

    def getPower(self, siUnit):
        return self.powers[siUnit]

    def toDimensionString(self):
        dimensionString = self.__getDimensionString()
        return '1' if len(dimensionString) is 0 else dimensionString;

    def equalsDimension(self, unit):
        return self.powers == unit.powers

    def isDimentionless(self):
        for power in self.powers:
            if power is not 0:
                return False
        return True

    def copy(self):
        return Unit(self.token, self.hasPrefix, self.factor, self.powers[0], self.powers[1], self.powers[2],
                    self.powers[3], self.powers[4], self.powers[5], self.powers[6])

    def toUnitString(self):
        result = ''
        if self.factor is not 1:
            result += str(self.factor)
        result += self.__getDimensionString()
        if len(result) is 0:
            return '1';
        return result

    def __repr__(self):
        return self.__str__()

    def __str__(self):
        return 'Unit[{},{},{},{}]'.format(self.token, self.hasPrefix, self.factor, self.toDimensionString())

    def __eq__(self, other):
        if isinstance(other, Unit):
            return (self.factor == other.factor) and (self.powers == other.powers)
        else:
            return False

    def __mul__(self, other):
        u = self.copy()
        u *= other
        return u

    def __imul__(self, other):
        u = self.copy()
        if isinstance(other, Unit):
            u.factor *= other.factor
            for i in range(len(u.powers)):
                u.powers[i] += other.powers[i]
        elif isinstance(other, int) or isinstance(other, float):
            u.factor *= other
        else:
            raise TypeError
        return u

    def __rmul__(self, other):
        return self.__mul__(other)

    def __ipow__(self, other):
        u = self.copy()
        if isinstance(other, int) or isinstance(other, float):
            u.factor **= other
            for i in range(len(u.powers)):
                u.powers[i] *= other
        else:
            raise TypeError
        return u

    def __pow__(self, power, modulo=None):
        u = self.copy()
        u **= power
        return u

    def __idiv__(self, other):
        u = self.copy()
        if isinstance(other, Unit):
            u.factor /= other.factor
            for i in range(len(u.powers)):
                u.powers[i] -= other.powers[i]
        elif isinstance(other, int) or isinstance(other, float):
            u.factor /= other
        else:
            raise TypeError
        return u

    def __truediv__(self, other):
        return self.__idiv__(other)

    def __rtruediv__(self, other):
        return Unit.DIMENSIONLESS / self * other

    def __getDimensionString(self):
        result = ''
        powerList = []
        for i in range(len(self.powers)):
            powerList.append((self.powers[i], i))
        powerList.sort(reverse=True)
        firstDimension = True
        for power, siBaseUnitIndex in powerList:
            siName = SIBaseUnit._value2member_map_[siBaseUnitIndex]._name_
            if power is not 0:
                if firstDimension:
                    firstDimension = False
                else:
                    result += '⋅'
                result += siName
                if power is not 1:
                    result += ('^{}'.format(power))
        return result

    '''Unit
  public Unit prefix(UnitPrefix prefix) throws QuantityException {
    if (this.hasPrefix) {
      return new UnitBuilder(this).setHasPrefix(false)
              .setFactor(factor * prefix.getFactor()).createUnit();
    }
    else {
      throw new QuantityException("Unit " + this.getToken() + " can not has prefix.");
    }
  }

  public void assertDimension(String dimension) throws UnitDimensionMissmatchException, QuantityParseException {
    assertDimension(of(dimension));
  }

  public void assertDimension(Unit dimension) throws UnitDimensionMissmatchException, QuantityParseException {
    if (!equalsDimension(dimension)) {
      throw new UnitDimensionMissmatchException("Assertion failed. Expected [" + dimension + "], get [" + toDimensionString() + "]");
    }
  }


  public static Unit U(String unitString) throws QuantityParseException {
    return new QuantityParser(unitString).parseUnit(true);
  }

}

'''
    unitMap = {}
    registerLock = threading.Lock()

    @staticmethod
    def register(unit):
        if not isinstance(unit, Unit):
            raise TypeError
        if (unit.token is None) or (len(unit.token) is 0):
            raise RuntimeError('Con not register an anonymous Unit.')
        Unit.registerLock.acquire()
        if Unit.unitMap.__contains__(unit.token):
            raise RuntimeError('Unit {} exists.'.format(unit.token))
        Unit.unitMap[unit.token] = unit
        Unit.registerLock.release()

    @staticmethod
    def getRegisteredUnits():
        return [u for u in Unit.unitMap.values()]

    @staticmethod
    def get(token):
        return Unit.unitMap.get(token, None)

    @staticmethod
    def create(token, hasPrefix=True, factor=1, powerM=0, powerKG=0, powerS=0, powerA=0, powerK=0, powerMOL=0,
               powerCD=0):
        unit = Unit(token, hasPrefix, factor, powerM, powerKG, powerS, powerA, powerK, powerMOL, powerCD)
        Unit.register(unit)
        return unit

    @staticmethod
    def of(unitString):
        return QuantityParser(unitString).parseUnit(True)


# UnitPrefixes
UnitPrefix.none = UnitPrefix.create("", "none")
UnitPrefix.deca = UnitPrefix.create("da", "deca", 1e1)
UnitPrefix.hecto = UnitPrefix.create("h", "hecto", 1e2)
UnitPrefix.kilo = UnitPrefix.create("k", "kilo", 1e3)
UnitPrefix.mega = UnitPrefix.create("M", "mega", 1e6)
UnitPrefix.giga = UnitPrefix.create("G", "giga", 1e9)
UnitPrefix.tera = UnitPrefix.create("T", "tera", 1e12)
UnitPrefix.peta = UnitPrefix.create("P", "peta", 1e15)
UnitPrefix.exa = UnitPrefix.create("E", "exa", 1e18)
UnitPrefix.zetta = UnitPrefix.create("Z", "zetta", 1e21)
UnitPrefix.yotta = UnitPrefix.create("Y", "yotta", 1e24)
UnitPrefix.deci = UnitPrefix.create("d", "deci", 1e-1)
UnitPrefix.centi = UnitPrefix.create("c", "centi", 1e-2)
UnitPrefix.milli = UnitPrefix.create("m", "milli", 1e-3)
UnitPrefix.micro = UnitPrefix.create("µ", "micro", 1e-6)
UnitPrefix.nano = UnitPrefix.create("n", "nano", 1e-9)
UnitPrefix.pico = UnitPrefix.create("p", "pico", 1e-12)
UnitPrefix.femto = UnitPrefix.create("f", "femto", 1e-15)
UnitPrefix.atto = UnitPrefix.create("a", "atto", 1e-18)
UnitPrefix.zepto = UnitPrefix.create("z", "zepto", 1e-21)
UnitPrefix.yocto = UnitPrefix.create("y", "yocto", 1e-24)

# SI Base Units
Unit.m = Unit.create("m", True, 1, 1, 0, 0, 0, 0, 0, 0)
Unit.g = Unit.create("g", True, 1e-3, 0, 1, 0, 0, 0, 0, 0)
Unit.s = Unit.create("s", True, 1, 0, 0, 1, 0, 0, 0, 0)
Unit.A = Unit.create("A", True, 1, 0, 0, 0, 1, 0, 0, 0)
Unit.K = Unit.create("K", True, 1, 0, 0, 0, 0, 1, 0, 0)
Unit.mol = Unit.create("mol", True, 1, 0, 0, 0, 0, 0, 1, 0)
Unit.cd = Unit.create("cd", True, 1, 0, 0, 0, 0, 0, 0, 1)

# Units
Unit.DIMENSIONLESS = Unit.create("DIMENSIONLESS", False, 1, 0, 0, 0, 0, 0, 0, 0)
Unit.rad = Unit.create("rad", True, 1, 0, 0, 0, 0, 0, 0, 0)
Unit.º = Unit.create("°", False, math.pi / 180, 0, 0, 0, 0, 0, 0, 0)
Unit.deg = Unit.create("deg", False, math.pi / 180, 0, 0, 0, 0, 0, 0, 0)
Unit.arcmin = Unit.create("′", False, math.pi / 180 / 60, 0, 0, 0, 0, 0, 0, 0)
Unit.arcsec = Unit.create("″", False, math.pi / 180 / 3600, 0, 0, 0, 0, 0, 0, 0)
Unit.sr = Unit.create("sr", True, 1, 0, 0, 0, 0, 0, 0, 0)
Unit.Hz = Unit.create("Hz", True, 1, 0, 0, -1, 0, 0, 0, 0)
Unit.N = Unit.create("N", True, 1, 1, 1, -2, 0, 0, 0, 0)
Unit.Pa = Unit.create("Pa", True, 1, -1, 1, -2, 0, 0, 0, 0)
Unit.J = Unit.create("J", True, 1, 2, 1, -2, 0, 0, 0, 0)
Unit.W = Unit.create("W", True, 1, 2, 1, -3, 0, 0, 0, 0)
Unit.C = Unit.create("C", True, 1, 0, 0, 1, 1, 0, 0, 0)
Unit.V = Unit.create("V", True, 1, 2, 1, -3, -1, 0, 0, 0)
Unit.F = Unit.create("F", True, 1, -2, -1, 4, 2, 0, 0, 0)
Unit.Ω = Unit.create("Ω", True, 1, 2, 1, -3, -2, 0, 0, 0)
Unit.Ohm = Unit.create("Ohm", True, 1, 2, 1, -3, -2, 0, 0, 0)
Unit.S = Unit.create("S", True, 1, -2, -1, 3, 2, 0, 0, 0)
Unit.Wb = Unit.create("Wb", True, 1, 2, 1, -2, -1, 0, 0, 0)
Unit.T = Unit.create("T", True, 1, 0, 1, -2, -1, 0, 0, 0)
Unit.H = Unit.create("H", True, 1, 2, 1, -2, -2, 0, 0, 0)
Unit.ºC = Unit.create("°C", False, 1, 0, 0, 0, 0, 1, 0, 0)
Unit.degC = Unit.create("degC", False, 1, 0, 0, 0, 0, 1, 0, 0)
Unit.lm = Unit.create("lm", True, 1, 0, 0, 0, 0, 0, 0, 1)
Unit.lx = Unit.create("lx", True, 1, -2, 0, 0, 0, 0, 0, 1)
Unit.Bq = Unit.create("Bq", True, 1, 0, 0, -1, 0, 0, 0, 0)
Unit.Gy = Unit.create("Gy", True, 1, 2, 0, -2, 0, 0, 0, 0)
Unit.Sv = Unit.create("Sv", True, 1, 2, 0, -2, 0, 0, 0, 0)
Unit.kat = Unit.create("kat", True, 1, 0, 0, -1, 0, 0, 1, 0)
Unit.d = Unit.create("d", False, 86400, 0, 0, 1, 0, 0, 0, 0)
Unit.h = Unit.create("h", False, 3600, 0, 0, 1, 0, 0, 0, 0)
Unit.min = Unit.create("min", False, 60, 0, 0, 1, 0, 0, 0, 0)
Unit.ºF = Unit.create("°F", False, 5. / 9, 0, 0, 0, 0, 1, 0, 0)
Unit.degF = Unit.create("degF", False, 5. / 9, 0, 0, 0, 0, 1, 0, 0)
Unit.cal = Unit.create("cal", True, 4.1868, 2, 1, -2, 0, 0, 0, 0)
Unit.eV = Unit.create("eV", True, 1.602176565e-19, 2, 1, -2, 0, 0, 0, 0)
Unit.Btu = Unit.create("Btu", False, 1055.056, 2, 1, -2, 0, 0, 0, 0)
Unit.erg = Unit.create("erg", True, 1e-7, 2, 1, -2, 0, 0, 0, 0)
Unit.dyn = Unit.create("dyn", True, 1e-5, 1, 1, -2, 0, 0, 0, 0)
Unit.lbf = Unit.create("lbf", False, 4.448222, 1, 1, -2, 0, 0, 0, 0)
Unit.inch = Unit.create("inch", False, 0.0254, 1, 0, 0, 0, 0, 0, 0)
Unit.ft = Unit.create("ft", False, 0.3048, 1, 0, 0, 0, 0, 0, 0)
Unit.mi = Unit.create("mi", False, 1609.344, 1, 0, 0, 0, 0, 0, 0)
Unit.atm = Unit.create("atm", False, 1.01325e5, -1, 1, -2, 0, 0, 0, 0)
Unit.bar = Unit.create("bar", True, 1e5, -1, 1, -2, 0, 0, 0, 0)
Unit.torr = Unit.create("torr", True, 133.3224, -1, 1, -2, 0, 0, 0, 0)
Unit.mmHg = Unit.create("mmHg", False, 133.3224, -1, 1, -2, 0, 0, 0, 0)
Unit.Ci = Unit.create("Ci", True, 3.7e10, 0, 0, -1, 0, 0, 0, 0)
Unit.acre = Unit.create("acre", False, 4046.864798, 2, 0, 0, 0, 0, 0, 0)
Unit.acr = Unit.create("acr", True, 100, 2, 0, 0, 0, 0, 0, 0)
Unit.nit = Unit.create("nit", True, 1, -2, 0, 0, 0, 0, 0, 1)
Unit.nits = Unit.create("nits", True, 1, -2, 0, 0, 0, 0, 0, 1)
Unit.sb = Unit.create("sb", True, 1e4, -2, 0, 0, 0, 0, 0, 1)
Unit.Mx = Unit.create("Mx", True, 1e-8, 2, 1, -2, -1, 0, 0, 0)
Unit.G = Unit.create("G", True, 1e-4, 0, 1, -2, -1, 0, 0, 0)
Unit.u = Unit.create("u", True, 1.660538921e-27, 0, 1, 0, 0, 0, 0, 0)
Unit.lb = Unit.create("lb", False, 0.45359237, 0, 1, 0, 0, 0, 0, 0)
Unit.slug = Unit.create("slug", False, 14.593903, 0, 1, 0, 0, 0, 0, 0)
Unit.hp = Unit.create("hp", False, 745.69987158227022, 2, 1, -3, 0, 0, 0, 0)
Unit.gal = Unit.create("gal", False, 0.003785411784, 3, 0, 0, 0, 0, 0, 0)
Unit.L = Unit.create("L", True, 0.001, 3, 0, 0, 0, 0, 0, 0)
Unit.pint = Unit.create("pint", False, 4.73176473e-4, 3, 0, 0, 0, 0, 0, 0)
Unit.qt = Unit.create("qt", False, 9.46352946e-4, 3, 0, 0, 0, 0, 0, 0)

'''PhysicalConstants
public class PhysicalConstants {

  /**
   * Dimensionless quantity of 1
   */
  public final static Quantity I = Quantity.of("1");

  /**
   * Speed of light in vacuum, c = 299792458m/s
   */
  public final static Quantity c = Quantity.of("299792458m/s");

  /**
   * Newtonian constant of gravitation, G = 6.67384e−11m^3/kg/s^2
   */
  public final static Quantity G = Quantity.of("6.67384e-11m^3/kg/s^2");

  /**
   * Planck constant, h = 6.62606957e−34J⋅s
   */
  public final static Quantity h = Quantity.of("6.62606957e-34J⋅s");

  /**
   * Reduced Planck constant, ℏ = 1.054571726e−34J⋅s
   */
  public final static Quantity ℏ = h.divide(2 * Math.PI);

  /**
   * Magnetic constant (vacuum permeability), µ_0 = 1.256637061e−6N/A^2
   */
  public final static Quantity µ_0 = Quantity.of("1e-7N/A^2").multiply(4 * Math.PI);

  /**
   * Electric constant (vacuum permittivity), ε_0 = 8.854187817e-12F/m
   */
  public final static Quantity ε_0 = I.divide(µ_0).divide(c.power(2));

  /**
   * Characteristic impedance of vacuum, Z_0 = 376.730313461Ω
   */
  public final static Quantity Z_0 = c.multiply(µ_0);

  /**
   * Coulomb's constant, k_e = 8.987551787e9N⋅m^2⋅C^-2
   */
  public final static Quantity k_e = I.divide(4 * Math.PI).divide(ε_0);

  /**
   * Elementary charge, e = 1.602176565e-19C
   */
  public final static Quantity e = Quantity.of("1.602176565e-19C");

  /**
   * Electron mass, m_e = 9.10938291e-31kg
   */
  public final static Quantity m_e = Quantity.of("9.10938291e-31kg");

  /**
   * Proton mass, m_p = 1.672621777e-27kg
   */
  public final static Quantity m_p = Quantity.of("1.672621777e-27kg");

  /**
   * Bohr magneton, µ_B = 9.27400968e-24J/T
   */
  public final static Quantity µ_B = e.multiply(ℏ).divide(2).divide(m_e);

  /**
   * Conductance quantum, G_0 = 7.7480917346e-5S
   */
  public final static Quantity G_0 = e.power(2).multiply(2).divide(h);

  /**
   * Josephson constant, K_J = 4.83597870e14Hz/V
   */
  public final static Quantity K_J = e.multiply(2).divide(h);

  /**
   * Magnetic flux quantum, ϕ_0 = 2.067833758e-15Wb
   */
  public final static Quantity ϕ_0 = h.divide(2).divide(e);

  /**
   * Nuclear magneton, µ_N = 5.05078353e-27J/T
   */
  public final static Quantity µ_N = e.multiply(ℏ).divide(2).divide(m_p);

  /**
   * Von Klitzing constant, R_K = 25812.8074434Ω
   */
  public final static Quantity R_K = h.divide(e.power(2));

  /**
   * Fine-structure constant, α = 7.2973525698e-3
   */
  public final static Quantity α = µ_0.multiply(e.power(2)).multiply(c).divide(2).divide(h);

  /**
   * Rydberg constant, R_∞ = 10973731.568539/m
   */
  public final static Quantity R_INFINITY = α.power(2).multiply(m_e).multiply(c).divide(2).divide(h);

  /**
   * Bohr radius, a_0 = 5.2917721092-11m
   */
  public final static Quantity a_0 = α.divide(4 * Math.PI).divide(R_INFINITY);

  /**
   * Classical electron radius, r_e = 2.8179403267e-15m
   */
  public final static Quantity r_e = e.power(2).divide(4 * Math.PI).divide(ε_0).divide(m_e).divide(c.power(2));

  /**
   * Hartree energy, E_h = 4.35974434e-18J
   */
  public final static Quantity E_h = R_INFINITY.multiply(2).multiply(h).multiply(c);

  /**
   * Gas constant, R = 8.3144621J/K/mol
   */
  public final static Quantity R = Quantity.of("8.3144621J/K/mol");

  /**
   * Avogadro's number, N_A = 6.02214129e23/mol
   */
  public final static Quantity N_A = Quantity.of("6.02214129e23/mol");

  /**
   * Boltzmann constant, k_B = 1.3806488e-23J/K
   */
  public final static Quantity k_B = R.divide(N_A);
}

'''

'''Quantity
package com.hwaipy.quantity;

import java.util.Objects;

/**
 *
 * @author Hwaipy
 */
public class Quantity implements Comparable<Quantity> {

  private final double value;
  private final Unit unit;

  public Quantity(double value, Unit unit) {
    this.value = value;
    this.unit = unit == null ? Units.DIMENSIONLESS : unit;
  }

  public double getValue() {
    return value;
  }

  public double getValueInSI() {
    return value * unit.getFactor();
  }

  public Unit getUnit() {
    return unit;
  }

  public double getValue(Unit unit) throws UnitDimensionMissmatchException {
    if (this.unit.equalsDimension(unit)) {
      return this.value * this.unit.getFactor() / unit.getFactor();
    }
    else {
      throw new UnitDimensionMissmatchException(this.unit.toDimensionString() + " can not be converted to " + unit.toDimensionString());
    }
  }

  public double getValue(String unit) throws UnitDimensionMissmatchException {
    return getValue(Unit.of(unit));
  }

  public Quantity plus(Quantity quantity) throws UnitDimensionMissmatchException {
    if (unit.equalsDimension(quantity.getUnit())) {
      double value2 = quantity.getValueInSI();
      return new Quantity(value + value2 / unit.getFactor(), unit);
    }
    else {
      throw new UnitDimensionMissmatchException(unit.toDimensionString() + "can not add with " + quantity.getUnit().toDimensionString());
    }
  }

  public Quantity minus(Quantity quantity) throws UnitDimensionMissmatchException {
    if (unit.equalsDimension(quantity.getUnit())) {
      double value2 = quantity.getValueInSI();
      return new Quantity(value - value2 / unit.getFactor(), unit);
    }
    else {
      throw new UnitDimensionMissmatchException(unit.toDimensionString() + "can not minus with " + quantity.getUnit().toDimensionString());
    }
  }

  public Quantity multiply(double d) {
    return new Quantity(value * d, unit);
  }

  public Quantity multiply(Quantity quantity) {
    return new Quantity(value * quantity.getValue(), unit.multiply(quantity.getUnit()));
  }

  public Quantity divide(double d) {
    return new Quantity(value / d, unit);
  }

  public Quantity divide(Quantity quantity) {
    return new Quantity(value / quantity.getValue(), unit.divide(quantity.getUnit()));
  }

  public Quantity power(int power) {
    return new Quantity(Math.pow(value, power), Units.DIMENSIONLESS.multiply(unit, power));
  }

  public Quantity sqrt() {
    return new Quantity(Math.sqrt(value), unit.sqrt());
  }

  public Quantity negate() {
    return new Quantity(-value, unit);
  }

  public Quantity reciprocal() {
    return new Quantity(1 / value, unit.reciprocal());
  }

  public void assertDimention(String dimention) {
    unit.assertDimension(dimention);
  }

  public void assertDimention(Unit unit) {
    this.unit.assertDimension(unit);
  }

  public boolean isDimentionless() {
    return unit.isDimentionless();
  }

  public static Quantity of(String quantityString) throws QuantityParseException {
    return new QuantityParser(quantityString).parse();
  }

  public static Quantity of(double dimensionlessQuantity) throws QuantityParseException {
    return new Quantity(dimensionlessQuantity, Units.DIMENSIONLESS);
  }

  public static Quantity Q(String quantityString) throws QuantityParseException {
    return new QuantityParser(quantityString).parse();
  }

  public static Quantity Q(double dimensionlessQuantity) throws QuantityParseException {
    return new Quantity(dimensionlessQuantity, Units.DIMENSIONLESS);
  }

  @Override
  public String toString() {
    return isDimentionless() ? "" + getValueInSI() : getValueInSI() + unit.toDimensionString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    Quantity quantity = (Quantity) obj;
    return equals(quantity);
  }

  public boolean equals(Quantity quantity) {
    return getUnit().equalsDimension(quantity.getUnit())
            && getValueInSI() == quantity.getValueInSI();
  }

  public boolean equals(Quantity quantity, double relative) {
    return equals(quantity, quantity.multiply(relative));
  }

  public boolean equals(Quantity quantity, Quantity absolute) {
    if (!getUnit().equalsDimension(quantity.getUnit()) || !getUnit().equalsDimension(absolute.getUnit())) {
      return false;
    }
    double v = getValueInSI();
    double q = quantity.getValueInSI();
    double a = Math.abs(absolute.getValueInSI());
    return v <= q + a && v >= q - a;
  }

  @Override
  public int hashCode() {
    int hash = 7;
    hash = 23 * hash + (int) (Double.doubleToLongBits(this.value) ^ (Double.doubleToLongBits(this.value) >>> 32));
    hash = 23 * hash + Objects.hashCode(this.unit);
    return hash;
  }

  @Override
  public int compareTo(Quantity o) {
    unit.assertDimension(o.unit);
    double diff = getValueInSI() - o.getValueInSI();
    return (diff < 0) ? -1 : ((diff == 0) ? 0 : 1);
  }

}

'''

'''QuantityException
package com.hwaipy.quantity;

/**
 *
 * @author Hwaipy 2015-3-17
 */
public class QuantityException extends RuntimeException {

  /**
   * Creates a new instance of <code>UnitException</code> without detail message.
   */
  public QuantityException() {
  }

  /**
   * Constructs an instance of <code>UnitException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public QuantityException(String msg) {
    super(msg);
  }

}

'''


class QuantityParseException(RuntimeError):
    def __init__(self, msg):
        self.msg = msg

    def __str__(self):
        return 'QuantityParseException: {}'.format(self.msg)


class QuantityParser:
    def __init__(self, quantityString):
        self.quantityString = quantityString
        self.position = 0

    def parse(self):
        quantity = self.tryExtractQuantity()
        if self.position < len(self.quantityString):
            raise QuantityParseException('')
        return quantity;

    def parseUnit(self, withFactor):
        factor = 1
        if withFactor:
            factor = self.tryExtractNumber()
            if (isNaN(factor)):
                factor = 1
        unit = self.tryExtractUnit()
        return UnitBuilder(unit).multiply(factor).createUnit()

    def tryExtractNumber(self):
        p0 = self.position
        integralString = self.tryExtractInteger(True)
        mantissaString = None
        powerString = None
        if self.isNext('.'):
            self.position += 1;
            mantissaString = self.tryExtractInteger(False)
        if ((len(integralString) is 0) or (
                    (len(integralString) is 1) and (('+' is integralString) or ('-' is integralString)))):
            if ((mantissaString is None) or (len(mantissaString) is 0)):
                self.position = p0
                return DoubleNaN
        valueString = integralString
        p1 = self.position
        if self.isNext('e') or self.isNext('E'):
            self.position += 1
            powerString = self.tryExtractInteger(True)
        if mantissaString is not None:
            valueString += "." + mantissaString
        if powerString is None:
            return float(valueString)
        elif ((len(powerString) is 0) or ((len(powerString) is 1) and (('+' is powerString) or ('-' is powerString)))):
            self.position = p1
            return float(valueString)
        else:
            return float(valueString + "E" + powerString);

    def tryExtractInteger(self, withSign):
        result = ''
        while self.position < len(self.quantityString):
            c = self.quantityString[self.position]
            if ((c is '+') or (c is '-')):
                if (withSign and (len(result) is 0)):
                    self.position += 1
                    result += c
                else:
                    break
            elif ((c is ',') or (c is ' ') or (c is '\t')):
                self.position += 1
            else:
                digit = self.digit(c)
                if digit > 0:
                    self.position += 1
                    result += chr(digit)
                else:
                    break
        return result

    def digit(self, c):
        if ((c >= '0') and (c <= '9')):
            return c
        elif ((c >= '０') and (c <= '９')):
            return (c - ('０' - '0'))
        else:
            return 0


'''QuantityParser




  public Unit tryExtractUnit() {
    UnitBuilder unitBuilder = new UnitBuilder();
    while (position < quantityString.length()) {
      trim();
      int p = position;
      boolean powerUp = true;
      char c = quantityString.charAt(position);
      if (c == '/') {
        powerUp = false;
        position++;
      }
      else if (c == '.' || c == '*' || c == '⋅' || c == '×') {
        position++;
      }
      Unit unit = tryExtractOneUnit();
      if (unit == null) {
        position = p;
        break;
      }
      else {
        if (powerUp) {
          unitBuilder.multiply(unit);
        }
        else {
          unitBuilder.divide(unit);
        }
      }
    }
    return unitBuilder.createUnit();
  }

  public Quantity tryExtractQuantity() {
    double value = tryExtractNumber();
    if (Double.isNaN(value)) {
      return null;
    }
    Unit unit = tryExtractUnit();
    return new Quantity(value, unit);
  }

  public String restString() {
    return quantityString.substring(position);
  }


  private boolean isNext(char target) {
    if (position < quantityString.length()) {
      char c = quantityString.charAt(position);
      return c == target;
    }
    else {
      return false;
    }
  }

  private char digit(char c) {
    if (c >= '0' && c <= '9') {
      return c;
    }
    else if (c >= '０' && c <= '９') {
      return (char) (c - ('０' - '0'));
    }
    else {
      return 0;
    }
  }

  private Unit tryExtractOneUnit() {
    int p0 = position;
    String token = tryExtractOneToken();
    if (token.length() == 0) {
      position = p0;
      return null;
    }
    else {
      Unit baseUnit = tryParseOneUnit(token);
      if (baseUnit == null) {
        position = p0;
        return null;
      }
      else {
        if (isNext('^')) {
          int p1 = position;
          position++;
          String powerString = tryExtractInteger(true);
          if (powerString.length() == 0 || (powerString.length() == 1 && ("+".equals(powerString) || "-".equals(powerString)))) {
            position = p1;
            return baseUnit;
          }
          else {
            int power = Integer.parseInt(powerString);
            return baseUnit.power(power);
          }
        }
        else {
          return baseUnit;
        }
      }
    }
  }

  private void trim() {
    while (position < quantityString.length()) {
      char c = quantityString.charAt(position);
      if (c == ' ' || c == '\t') {
        position++;
      }
      else {
        break;
      }
    }
  }

  private String tryExtractOneToken() {
    StringBuilder sb = new StringBuilder();
    while (position < quantityString.length()) {
      char c = quantityString.charAt(position);
      if (c == ' ' || c == '\t' || c == '+' || c == '-' || c == ',' || c == '.'
              || c == '*' || c == '⋅' || c == '×' || c == '/' || c == '^') {
      }
      else {
        switch (Character.getType(c)) {
          case Character.COMBINING_SPACING_MARK:
          case Character.CONNECTOR_PUNCTUATION:
          case Character.CURRENCY_SYMBOL:
          case Character.DASH_PUNCTUATION:
          case Character.ENCLOSING_MARK:
          case Character.START_PUNCTUATION:
          case Character.END_PUNCTUATION:
          case Character.FINAL_QUOTE_PUNCTUATION:
          case Character.FORMAT:
          case Character.INITIAL_QUOTE_PUNCTUATION:
          case Character.LETTER_NUMBER:
          case Character.LOWERCASE_LETTER:
          case Character.MATH_SYMBOL:
          case Character.MODIFIER_LETTER:
          case Character.MODIFIER_SYMBOL:
          case Character.NON_SPACING_MARK:
          case Character.OTHER_LETTER:
          case Character.OTHER_NUMBER:
          case Character.OTHER_PUNCTUATION:
          case Character.OTHER_SYMBOL:
          case Character.SURROGATE:
          case Character.TITLECASE_LETTER:
          case Character.UPPERCASE_LETTER:
            position++;
            sb.append(c);
            continue;
          default:
        }
      }
      break;
    }
    return sb.toString();
  }

  private Unit tryParseOneUnit(String unitString) {
    for (int split = 0; split < unitString.length(); split++) {
      String prefixString = unitString.substring(0, split);
      String tokenString = unitString.substring(split);
      UnitPrefix prefix = UnitPrefixes.get(prefixString);
      Unit unit = Units.get(tokenString);
      if (prefix != null && unit != null && (prefix == UnitPrefixes.none || unit.hasPrefix())) {
        return unit.prefix(prefix);
      }
    }
    return null;
  }
}

'''

'''UnitDimensionMissmatchException
package com.hwaipy.quantity;

/**
 *
 * @author Hwaipy 2015-3-17
 */
public class UnitDimensionMissmatchException extends QuantityException {

  /**
   * Creates a new instance of <code>UnitDimensionMissmatchException</code> without detail message.
   */
  public UnitDimensionMissmatchException() {
  }

  /**
   * Constructs an instance of <code>UnitDimensionMissmatchException</code> with the specified detail message.
   * @param msg the detail message.
   */
  public UnitDimensionMissmatchException(String msg) {
    super(msg);
  }

}

'''

import unittest


class QuantityParserTest(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testTryExtractNumber(self):
        quantityParser = QuantityParser(".11eV");
        self.assertEqual(.11, quantityParser.tryExtractNumber());
        '''
    assertEquals("eV", quantityParser.restString());
    //R1
    quantityParser = new QuantityParser("-+ 12,,,, . e 1 e 1,2312,  ,1,00 e V");
    assertEquals(Double.NaN, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("-+ 12,,,, . e 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    quantityParser = new QuantityParser("-.  + 12,,,, . e 1 e 1,2312,  ,1,00 e V");
    assertEquals(Double.NaN, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("-.  + 12,,,, . e 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    quantityParser = new QuantityParser("-.+ 12,,,, . e 1 e 1,2312,  ,1,00 e V");
    assertEquals(Double.NaN, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("-.+ 12,,,, . e 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    //R2
    quantityParser = new QuantityParser("  12,,,,  A 1 e 1,2312,  ,1,00 e V");
    assertEquals(12, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("A 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    //IF1R2
    quantityParser = new QuantityParser("  12,,,,.  A 1 e 1,2312,  ,1,00 e V");
    assertEquals(12, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("A 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    //IF0R3
    quantityParser = new QuantityParser("  12,,,,  1 eA 1 e 1,2312,  ,1,00 e V");
    assertEquals(121, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("eA 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    //IF0IF1R3
    quantityParser = new QuantityParser("  12,,,, . eA 1 e 1,2312,  ,1,00 e V");
    assertEquals(12, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("eA 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    //IF0R4
    quantityParser = new QuantityParser("  12,,,,e 1A 1 e 1,2312,  ,1,00 e V");
    assertEquals(120, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("A 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    //IF0IF1R4
    quantityParser = new QuantityParser("  12,,,,.e 1A 1 e 1,2312,  ,1,00 e V");
    assertEquals(120, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("A 1 e 1,2312,  ,1,00 e V", quantityParser.restString());
    //
    quantityParser = new QuantityParser("  12.e-A");
    assertEquals(12, quantityParser.tryExtractNumber(), 0.0000001);
    assertEquals("e-A", quantityParser.restString());
    '''

    def testTryExtractUnit(self):
        pass
        '''
    QuantityParser quantityParser;
    quantityParser = new QuantityParser("  Wb");
    assertTrue(Units.Wb.equalsDimension(quantityParser.tryExtractUnit()));
    assertEquals("", quantityParser.restString());
    quantityParser = new QuantityParser("  /s J.");
    assertTrue(Units.W.equalsDimension(quantityParser.tryExtractUnit()));
    assertEquals(".", quantityParser.restString());
    quantityParser = new QuantityParser("  /m lm.m^-1");
    assertTrue(Units.lx.equalsDimension(quantityParser.tryExtractUnit()));
    assertEquals("", quantityParser.restString());
    '''

    def testTryExtractQuantity(self):
        pass
        '''
    QuantityParser quantityParser;
    quantityParser = new QuantityParser("  .kg");
    assertNull(quantityParser.tryExtractQuantity());
    assertEquals("  .kg", quantityParser.restString());
    quantityParser = new QuantityParser(" 1 .kg");
    assertTrue(new Quantity(1000, Units.g).equals(quantityParser.tryExtractQuantity(), 0.001));
    assertEquals("", quantityParser.restString());
    '''


class QuantityTest(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testCalculation(self):
        pass
        '''
            Quantity timeStart = new Quantity(30, s);
    Quantity timeStop = new Quantity(35, s);
    Quantity work = new Quantity(50000, J);

    Quantity duration = timeStop.minus(timeStart);
    assertEquals(5, duration.getValueInSI(), 0.0);
    assertEquals(5, duration.getValue(s), 0.0);
    assertEquals(5000000000.0, duration.getValue(s.prefix(nano)), 0.0);
    assertEquals(0.005, duration.getValue(s.prefix(kilo)), 0.0);

                '''

    def testParser(self):
        pass
        '''
    Collection<Unit> registeredUnits = Units.getRegisteredUnits();
    for (Unit unit : registeredUnits) {
      Unit parsedUnit = new QuantityParser(unit.toUnitString()).parseUnit(true);
      assertTrue(unit.equalsDimension(parsedUnit));
      assertEquals(unit.getFactor(), parsedUnit.getFactor(), 0.0);
    }
    assertTrue(new Quantity(3e8, m.divide(s)).equals(Quantity.of("3e8m/s"), 0.00001));
    assertTrue(new Quantity(16.7e-12, m.multiply(A)).equals(Quantity.of("16.7E0mm*nA"), 0.00001));
    assertTrue(new Quantity(16.7e-12, m.multiply(A)).equals(Quantity.of("16.7E0mm nA"), 0.00001));
    assertTrue(new Quantity(16.7e-12, m.multiply(A)).equals(Quantity.of("16.7E0mm.nA"), 0.00001));
    assertTrue(new Quantity(16.7e-12, m.multiply(A)).equals(Quantity.of("16.7E0 mm.nA"), 0.00001));
    assertTrue(new Quantity(16e-6, A.divide(m)).equals(Quantity.of("16E0/mm.nA"), 0.00001));
    assertEquals(new Quantity(1, Units.DIMENSIONLESS), Quantity.of("1"));
    assertEquals(null, Quantity.of(""));

    try {
      Quantity.of("123eV.");
      fail();
    } catch (QuantityParseException e) {
    }
  }'''

    def testAssertDimention(self):
        pass
        '''
    PhysicalConstants.c.assertDimention("m/s");
    '''

    def testAssertDimentionException(self):
        pass
        '''
  @Test(expected = UnitDimensionMissmatchException.class)
  public void testAssertDimentionException() {
    PhysicalConstants.c.assertDimention("m");
  }'''


class UnitTest(unittest.TestCase):
    def setUp(self):
        pass

    def tearDown(self):
        pass

    def testToUnitStringAndToDimensionList(self):
        unitList = []
        unitDList = []
        unitList.append((Unit.m, "", "m"))
        unitList.append((Unit.g, "0.001", "kg"))
        unitList.append((Unit.s, "", "s"))
        unitList.append((Unit.A, "", "A"))
        unitList.append((Unit.K, "", "K"))
        unitList.append((Unit.mol, "", "mol"))
        unitList.append((Unit.cd, "", "cd"))
        unitList.append((Unit.DIMENSIONLESS, "", "1"))
        unitList.append((Unit.rad, "", "1"))
        unitDList.append((Unit.º, "0.017453292519943295", "1"))
        unitDList.append((Unit.deg, "0.017453292519943295", "1"))
        unitDList.append((Unit.arcmin, "0.0002908882086657216", "1"))
        unitDList.append((Unit.arcsec, "4.84813681109536e-06", "1"))
        unitList.append((Unit.sr, "", "1"))
        unitList.append((Unit.Hz, "", "s^-1"))
        unitList.append((Unit.N, "", "kg⋅m⋅s^-2"))
        unitList.append((Unit.Pa, "", "kg⋅m^-1⋅s^-2"))
        unitList.append((Unit.J, "", "m^2⋅kg⋅s^-2"))
        unitList.append((Unit.W, "", "m^2⋅kg⋅s^-3"))
        unitList.append((Unit.C, "", "A⋅s"))
        unitList.append((Unit.V, "", "m^2⋅kg⋅A^-1⋅s^-3"))
        unitList.append((Unit.F, "", "s^4⋅A^2⋅kg^-1⋅m^-2"))
        unitList.append((Unit.Ω, "", "m^2⋅kg⋅A^-2⋅s^-3"))
        unitList.append((Unit.Ohm, "", "m^2⋅kg⋅A^-2⋅s^-3"))
        unitList.append((Unit.S, "", "s^3⋅A^2⋅kg^-1⋅m^-2"))
        unitList.append((Unit.Wb, "", "m^2⋅kg⋅A^-1⋅s^-2"))
        unitList.append((Unit.T, "", "kg⋅A^-1⋅s^-2"))
        unitList.append((Unit.H, "", "m^2⋅kg⋅A^-2⋅s^-2"))
        unitList.append((Unit.ºC, "", "K"))
        unitList.append((Unit.degC, "", "K"))
        unitList.append((Unit.lm, "", "cd"))
        unitList.append((Unit.lx, "", "cd⋅m^-2"))
        unitList.append((Unit.Bq, "", "s^-1"))
        unitList.append((Unit.Gy, "", "m^2⋅s^-2"))
        unitList.append((Unit.Sv, "", "m^2⋅s^-2"))
        unitList.append((Unit.kat, "", "mol⋅s^-1"))
        unitList.append((Unit.d, "86400", "s"))
        unitList.append((Unit.h, "3600", "s"))
        unitList.append((Unit.min, "60", "s"))
        unitList.append((Unit.ºF, "0.5555555555555556", "K"))
        unitList.append((Unit.degF, "0.5555555555555556", "K"))
        unitList.append((Unit.cal, "4.1868", "m^2⋅kg⋅s^-2"))
        unitList.append((Unit.eV, "1.602176565e-19", "m^2⋅kg⋅s^-2"))
        unitList.append((Unit.Btu, "1055.056", "m^2⋅kg⋅s^-2"))
        unitList.append((Unit.erg, "1e-07", "m^2⋅kg⋅s^-2"))
        unitList.append((Unit.dyn, "1e-05", "kg⋅m⋅s^-2"))
        unitList.append((Unit.lbf, "4.448222", "kg⋅m⋅s^-2"))
        unitList.append((Unit.inch, "0.0254", "m"))
        unitList.append((Unit.ft, "0.3048", "m"))
        unitList.append((Unit.mi, "1609.344", "m"))
        unitList.append((Unit.atm, "101325.0", "kg⋅m^-1⋅s^-2"))
        unitList.append((Unit.bar, "100000.0", "kg⋅m^-1⋅s^-2"))
        unitList.append((Unit.torr, "133.3224", "kg⋅m^-1⋅s^-2"))
        unitList.append((Unit.mmHg, "133.3224", "kg⋅m^-1⋅s^-2"))
        unitList.append((Unit.Ci, "37000000000.0", "s^-1"))
        unitList.append((Unit.acre, "4046.864798", "m^2"))
        unitList.append((Unit.acr, "100", "m^2"))
        unitList.append((Unit.nit, "", "cd⋅m^-2"))
        unitList.append((Unit.nits, "", "cd⋅m^-2"))
        unitList.append((Unit.sb, "10000.0", "cd⋅m^-2"))
        unitList.append((Unit.Mx, "1e-08", "m^2⋅kg⋅A^-1⋅s^-2"))
        unitList.append((Unit.G, "0.0001", "kg⋅A^-1⋅s^-2"))
        unitList.append((Unit.u, "1.660538921e-27", "kg"))
        unitList.append((Unit.lb, "0.45359237", "kg"))
        unitList.append((Unit.slug, "14.593903", "kg"))
        unitList.append((Unit.hp, "745.6998715822702", "m^2⋅kg⋅s^-3"))
        unitList.append((Unit.gal, "0.003785411784", "m^3"))
        unitList.append((Unit.L, "0.001", "m^3"))
        unitList.append((Unit.pint, "0.000473176473", "m^3"))
        unitList.append((Unit.qt, "0.000946352946", "m^3"))
        for triple in unitList:
            self.assertEqual(triple[2], triple[0].toDimensionString())
            self.assertEqual(triple[1] + triple[2], triple[0].toUnitString())
        for triple in unitDList:
            self.assertEqual(triple[2], triple[0].toDimensionString())
            self.assertEqual(triple[1], triple[0].toUnitString())

    def testCalculationList(self):
        u = Unit.V.copy()
        u *= Unit.Hz
        self.assertEqual(Unit('', False, 1, 2, 1, -4, -1, 0, 0, 0), u)
        u *= 10
        self.assertEqual(Unit('', False, 10, 2, 1, -4, -1, 0, 0, 0), u)
        self.assertEqual(Unit('', False, 1, 2, 1, -4, -1, 0, 0, 0), Unit.Hz * Unit.V)
        self.assertEqual(Unit('', False, 10, 2, 1, -4, -1, 0, 0, 0), Unit.Hz * Unit.V * 10)
        self.assertEqual(Unit('', False, 1.1, 2, 1, -4, -1, 0, 0, 0), 1.1 * Unit.Hz * Unit.V)
        self.assertEqual(Unit('', False, 1, 4, 2, -6, -2, 0, 0, 0), Unit.V ** 2)
        u = Unit.V
        u **= 3
        self.assertEqual(Unit('', False, 1, 6, 3, -9, -3, 0, 0, 0), u)
        u /= 2
        self.assertEqual(Unit('', False, 0.5, 6, 3, -9, -3, 0, 0, 0), u)
        u /= Unit.s
        self.assertEqual(Unit('', False, 0.5, 6, 3, -10, -3, 0, 0, 0), u)
        self.assertEqual(Unit('', False, 0.5, 6, 3, -11, -3, 0, 0, 0), u / Unit.s)
        self.assertEqual(Unit('', False, 4, -6, -3, 10, 3, 0, 0, 0), 2 / u)
        self.assertEqual(Unit('', False, 1 / 60, 2, 1, -4, -1, 0, 0, 0), Unit.V / Unit.min)

    def testParser(self):
        unitPairs = []
        # unitPairs.append((Unit.d, Unit.of("86400s")))
        '''
    unitPairs.add(ImmutablePair.of(eV, Unit.of("1.602176565e-19m^2*kg/s^2")));
    unitPairs.add(ImmutablePair.of(eV, Unit.of(".000001MeV")));
    unitPairs.add(ImmutablePair.of(mmHg, Unit.of("133.3224kg/m/s/s")));
    unitPairs.add(ImmutablePair.of(mmHg, Unit.of("133.3224*kg/m/s/s")));
    unitPairs.add(ImmutablePair.of(mmHg, Unit.of("133.3224/m*kg/s/s")));
    unitPairs.add(ImmutablePair.of(mmHg, Unit.of("133.3224/m*kg/s/s")));
    unitPairs.add(ImmutablePair.of(T, Unit.of("kg/s/s/A")));
    unitPairs.add(ImmutablePair.of(T, Unit.of("T")));
    unitPairs.add(ImmutablePair.of(T.prefix(micro), Unit.of("µT")));
    for (Pair<Unit, Unit> unitPair : unitPairs) {
      assertTrue(unitPair.getLeft().equalsDimension(unitPair.getRight()));
      assertEquals(unitPair.getLeft().getFactor(), unitPair.getRight().getFactor(), 0.0);
    }

        '''


if __name__ == '__main__':
    print('Test Quantity')
    unittest.main()
