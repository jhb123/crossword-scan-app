package com.jhb.crosswordScan

import com.jhb.crosswordScan.data.Cell
import org.junit.Assert
import org.junit.Test

class TestCell {
    @Test
    fun check_equality() {
        val cell_a = Cell(x=0,y=0,c="")
        val cell_b = Cell(x=0,y=0,c="")
        Assert.assertEquals(true, cell_a == cell_b)
    }

    @Test
    fun check_hash() {
        val cell_a = Cell(x=0,y=0,c="")
        val cell_b = Cell(x=0,y=0,c="")
        Assert.assertEquals(true, cell_a.hashCode() == cell_b.hashCode())
    }

    @Test
    fun check_serialisation() {
        val cell_a = Cell(x=0,y=0,c="a")
        Assert.assertEquals("{\"x\":0,\"y\":0,\"c\":\"a\"}", cell_a.toString())
    }
}