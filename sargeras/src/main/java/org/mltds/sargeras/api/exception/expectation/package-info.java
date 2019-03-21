/**
 * 可预期的异常， 用来控制 Saga 的走向<br/>
 * 因为有些业务系统已经有了自己的异常体系，所以提供了接口形式和类形式的定义，可以自行选择是继承（extends) 或者为实现（implement）。
 *
 * @author sunyi.
 */
package org.mltds.sargeras.api.exception.expectation;